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
            Path envPath = null;
            Path currentDir = Paths.get("").toAbsolutePath();
            
            // Priority 1: Try parent directory (root project) - where .env usually is
            Path parentDir = currentDir.getParent();
            if (parentDir != null) {
                envPath = parentDir.resolve(".env");
                if (!Files.exists(envPath)) {
                    envPath = null;
                }
            }
            
            // Priority 2: Try current directory (backend/)
            if (envPath == null) {
                envPath = currentDir.resolve(".env");
                if (!Files.exists(envPath)) {
                    envPath = null;
                }
            }
            
            // Priority 3: Try relative path
            if (envPath == null) {
                envPath = Paths.get("..", ".env").normalize();
                if (!Files.exists(envPath)) {
                    envPath = null;
                }
            }
            
            if (envPath != null && Files.exists(envPath)) {
                Path envDir = envPath.getParent();
                Dotenv dotenv = Dotenv.configure()
                        .directory(envDir != null ? envDir.toString() : ".")
                        .filename(".env")
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
                    log.info("Loaded {} variables from .env file at {}", loaded, envPath.toAbsolutePath());
                } else {
                    log.debug("No new variables loaded from .env (all already set)");
                }
            } else {
                log.warn(".env file not found. Searched in: currentDir={}, parentDir={}", 
                        currentDir, parentDir);
            }
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}", e.getMessage(), e);
        }
    }
}
