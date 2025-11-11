package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.repository.FlightSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    // Daily run: T+1 snapshots by default
    @Scheduled(cron = "${app.ingestion.cron:0 15 2 * * *}")
    public void runDailySnapshotIngestion() {
        if (!ingestionEnabled) {
            log.debug("Flight ingestion disabled (app.ingestion.enabled=false)");
            return;
        }
        log.info("Starting flight ingestion job with provider={}", provider);
        try {
            LocalDate date = LocalDate.now().plusDays(1);
            Map<String, FlightIngestionProvider> byName = providers.stream()
                    .collect(Collectors.toMap(FlightIngestionProvider::name, p -> p));
            FlightIngestionProvider selected = byName.get(provider);
            if (selected == null) {
                log.warn("No ingestion provider found for name='{}'", provider);
                return;
            }
            // Fetch → normalize → persist snapshots
            List<FlightSnapshot> snapshots = selected.fetchDaily(date);
            if (snapshots == null || snapshots.isEmpty()) {
                log.info("No snapshots fetched for date {}", date);
                return;
            }
            snapshotRepository.saveAll(snapshots);
            log.info("Ingestion completed: saved {} snapshots for {}", snapshots.size(), date);
        } catch (Exception e) {
            log.error("Ingestion failed: {}", e.getMessage(), e);
        }
    }
}


