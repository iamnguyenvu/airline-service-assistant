package io.github.nguyenvu.backend;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(BackendApplication.class, args);
    }

    private static void loadDotEnv() {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(".")
                        .ignoreIfMissing()
                        .load();
                int loaded = 0;
                for (var entry : dotenv.entries()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (System.getenv(key) == null && System.getProperty(key) == null) {
                        System.setProperty(key, value);
                        loaded++;
                        log.debug("Loaded from .env: {}={}", key, value.length() > 20 ? value.substring(0, 20) + "..." : "***");
                    } else {
                        log.debug("Skipped .env entry {} (already set)", key);
                    }
                }
                if (loaded > 0) {
                    log.info("Loaded {} variables from .env file", loaded);
                }
            } else {
                log.debug(".env file not found in current directory, skipping");
            }
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}", e.getMessage());
        }
    }
}
