package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.repository.FlightSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightIngestionService {

    @Value("${app.ingestion.enabled:false}")
    private boolean ingestionEnabled;

    @Value("${app.ingestion.provider:mock}")
    private String provider;

    private final List<FlightIngestionProvider> providers;
    private final FlightSnapshotRepository snapshotRepository;

    private Map<String, FlightIngestionProvider> providerMap;

    // Track last successful ingestion for health monitoring
    private final AtomicReference<LocalDateTime> lastSuccessfulIngestion = new AtomicReference<>();
    private final AtomicReference<String> lastIngestionStatus = new AtomicReference<>("NOT_RUN");

    @jakarta.annotation.PostConstruct
    void init() {
        providerMap = providers.stream()
                .collect(Collectors.toMap(p -> p.name().toLowerCase(), p -> p));
        log.info("FlightIngestionService initialized with {} providers: {}",
                providerMap.size(), providerMap.keySet());
    }

    private FlightIngestionProvider resolveProvider(String name) {
        if (providerMap == null) {
            init();
        }
        FlightIngestionProvider selected = providerMap.get(name.toLowerCase());
        if (selected == null) {
            log.warn("No ingestion provider found for name='{}'", name);
        }
        return selected;
    }

    public List<FlightSnapshot> testFetch(String providerName, LocalDate date, String dep, String arr) {
        FlightIngestionProvider selected = resolveProvider(providerName);
        if (selected == null) {
            log.warn("Provider '{}' not found. Available providers: {}", providerName,
                    providerMap != null ? providerMap.keySet() : "none");
            throw new IllegalArgumentException("Provider '" + providerName + "' not found");
        }
        try {
            if (dep != null && arr != null) {
                return selected.fetchRoute(date, dep, arr);
            }
            return selected.fetchDaily(date);
        } catch (IllegalStateException e) {
            log.warn("Provider '{}' configuration error: {}", providerName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching from provider '{}': {}", providerName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch from provider '" + providerName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Daily ingestion job for tomorrow's flights (T+1).
     * Runs at 2:15 AM Vietnam time.
     */
    @Scheduled(cron = "${app.ingestion.cron:0 15 2 * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    @CacheEvict(value = "todayFlights", allEntries = true)
    public void runDailySnapshotIngestion() {
        if (!ingestionEnabled) {
            log.debug("Flight ingestion disabled (app.ingestion.enabled=false)");
            return;
        }
        LocalDate date = LocalDate.now().plusDays(1);
        log.info("[SCHEDULER] Starting T+1 flight ingestion for {} with provider={}", date, provider);
        ingestForDate(date);
    }

    /**
     * Morning ingestion job for today's flights.
     * Runs at 6:00 AM Vietnam time to ensure today's data is available.
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    @CacheEvict(value = "todayFlights", allEntries = true)
    public void runTodayFlightIngestion() {
        if (!ingestionEnabled) {
            log.debug("Flight ingestion disabled (app.ingestion.enabled=false)");
            return;
        }
        LocalDate today = LocalDate.now();
        log.info("[SCHEDULER] Starting today's flight ingestion for {} with provider={}", today, provider);
        ingestForDate(today);
    }

    /**
     * Health check job - verifies data availability.
     * Runs every hour to monitor data freshness.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void verifyDataHealth() {
        LocalDate today = LocalDate.now();
        long todayCount = snapshotRepository.countBySnapshotDate(today);
        long tomorrowCount = snapshotRepository.countBySnapshotDate(today.plusDays(1));

        if (todayCount == 0 && ingestionEnabled) {
            log.warn("[HEALTH] ⚠️ No flights found for TODAY ({}). Consider manual ingestion.", today);
            lastIngestionStatus.set("WARNING_NO_TODAY_DATA");
        } else if (tomorrowCount == 0 && ingestionEnabled) {
            log.info("[HEALTH] No T+1 flights yet for {}. Will be ingested at 2:15 AM.", today.plusDays(1));
        } else {
            log.debug("[HEALTH] ✅ Data OK: {} flights for today, {} for tomorrow", todayCount, tomorrowCount);
        }
    }

    /**
     * Ingest flights for a specific date.
     */
    @Transactional
    public void ingestForDate(LocalDate date) {
        try {
            FlightIngestionProvider selected = resolveProvider(provider);
            if (selected == null) {
                lastIngestionStatus.set("FAILED_NO_PROVIDER");
                return;
            }

            // Fetch → normalize → persist snapshots
            List<FlightSnapshot> snapshots = selected.fetchDaily(date);
            if (snapshots == null || snapshots.isEmpty()) {
                log.info("[INGESTION] No snapshots fetched for date {}", date);
                lastIngestionStatus.set("COMPLETED_EMPTY");
                return;
            }

            snapshotRepository.saveAll(snapshots);
            lastSuccessfulIngestion.set(LocalDateTime.now());
            lastIngestionStatus.set("SUCCESS");
            log.info("[INGESTION] ✅ Completed: saved {} snapshots for {}", snapshots.size(), date);
        } catch (Exception e) {
            lastIngestionStatus.set("FAILED_" + e.getClass().getSimpleName());
            log.error("[INGESTION] ❌ Failed for {}: {}", date, e.getMessage(), e);
        }
    }

    /**
     * Get ingestion health status for monitoring.
     */
    public Map<String, Object> getHealthStatus() {
        LocalDate today = LocalDate.now();
        return Map.of(
                "enabled", ingestionEnabled,
                "provider", provider,
                "lastSuccessfulIngestion", lastSuccessfulIngestion.get() != null
                        ? lastSuccessfulIngestion.get().toString()
                        : "never",
                "lastStatus", lastIngestionStatus.get(),
                "todayFlightCount", snapshotRepository.countBySnapshotDate(today),
                "tomorrowFlightCount", snapshotRepository.countBySnapshotDate(today.plusDays(1)));
    }
}
