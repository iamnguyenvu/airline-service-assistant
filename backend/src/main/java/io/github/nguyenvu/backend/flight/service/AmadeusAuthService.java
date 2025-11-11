package io.github.nguyenvu.backend.flight.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusAuthService {

    private final RestTemplate restTemplate;

    @Value("${app.ingestion.amadeus.client-id:}")
    private String clientId;

    @Value("${app.ingestion.amadeus.client-secret:}")
    private String clientSecret;

    @Value("${app.ingestion.amadeus.base-url:https://test.api.amadeus.com}")
    private String baseUrl;

    private final Object lock = new Object();
    private volatile TokenHolder cachedToken;

    public boolean isConfigured() {
        boolean configured = clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
        if (!configured) {
            log.debug("Amadeus credentials check: clientId={}, clientSecret={}", 
                    clientId != null && !clientId.isBlank() ? "***" : "MISSING",
                    clientSecret != null && !clientSecret.isBlank() ? "***" : "MISSING");
        }
        return configured;
    }

    public String getAccessToken() {
        if (!isConfigured()) {
            throw new IllegalStateException("Amadeus client credentials are not configured");
        }
        TokenHolder current = cachedToken;
        if (current != null && current.valid()) {
            return current.accessToken();
        }
        synchronized (lock) {
            current = cachedToken;
            if (current != null && current.valid()) {
                return current.accessToken();
            }
            TokenHolder refreshed = requestToken();
            cachedToken = refreshed;
            return refreshed.accessToken();
        }
    }

    private TokenHolder requestToken() {
        String url = baseUrl + "/v1/security/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(url, request, TokenResponse.class);
            TokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || tokenResponse.accessToken == null) {
                log.error("Amadeus token response is null or missing access_token. Status: {}, Body: {}", 
                        response.getStatusCode(), response.getBody());
                throw new IllegalStateException("Failed to obtain Amadeus access token: response is null or missing access_token");
            }
            long expiresIn = tokenResponse.expiresIn != null ? tokenResponse.expiresIn : 1800;
            Instant expiresAt = Instant.now().plusSeconds(expiresIn - 30);
            log.info("Obtained Amadeus token (expires in {}s)", expiresIn);
            return new TokenHolder(tokenResponse.accessToken, expiresAt);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Amadeus token request failed with HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Failed to obtain Amadeus access token: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Amadeus token request failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to obtain Amadeus access token: " + e.getMessage(), e);
        }
    }

    private record TokenHolder(String accessToken, Instant expiresAt) {
        boolean valid() {
            return expiresAt != null && Instant.now().isBefore(expiresAt);
        }
    }

    private static class TokenResponse {
        private String type;
        private String accessToken;
        private Long expiresIn;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}


