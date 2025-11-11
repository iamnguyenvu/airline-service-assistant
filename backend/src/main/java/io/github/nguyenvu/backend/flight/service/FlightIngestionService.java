package io.github.nguyenvu.backend.flight.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightIngestionService {

    @Value("${app.ingestion.enabled:false}")
    private boolean ingestionEnabled;

    @Value("${app.ingestion.provider:mock}")
    private String provider;

    @Scheduled(cron = "${app.ingestion.cron:0 15 2 * * *}")
    public void runDailySnapshotIngestion() {
        if (!ingestionEnabled) {
            log.debug("Flight ingestion disabled (app.ingestion.enabled=false)");
            return;
        }
        log.info("Starting flight ingestion job with provider={}", provider);
        try {
            log.info("Ingestion completed (placeholder)");
        } catch (Exception e) {
            log.error("Ingestion failed: {}", e.getMessage(), e);
        }
    }
}


