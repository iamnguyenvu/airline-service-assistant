package io.github.nguyenvu.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple rate limiting configuration using sliding window algorithm.
 * No external dependencies required - pure Java implementation.
 * 
 * Enable with: app.rate-limit.enabled=true
 * Configure with: app.rate-limit.requests-per-minute=60
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true")
public class RateLimitConfig {

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    // Store rate limit state per IP address
    private final Map<String, RateLimitState> rateLimitStates = new ConcurrentHashMap<>();

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }

    /**
     * Simple rate limit state using sliding window counter.
     */
    private static class RateLimitState {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        private final int maxRequests;
        private static final long WINDOW_MS = 60_000; // 1 minute window

        RateLimitState(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long windowStartTime = windowStart.get();

            // Reset window if expired
            if (now - windowStartTime > WINDOW_MS) {
                count.set(0);
                windowStart.set(now);
            }

            // Try to acquire
            if (count.get() < maxRequests) {
                count.incrementAndGet();
                return true;
            }
            return false;
        }

        int getRemaining() {
            return Math.max(0, maxRequests - count.get());
        }
    }

    public class RateLimitFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            String path = request.getRequestURI();

            // Skip rate limiting for health checks and docs
            if (path.startsWith("/actuator/") ||
                    path.startsWith("/swagger-ui/") ||
                    path.startsWith("/v3/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }

            String clientIP = getClientIP(request);
            RateLimitState state = rateLimitStates.computeIfAbsent(clientIP,
                    k -> new RateLimitState(requestsPerMinute));

            if (state.tryAcquire()) {
                response.addHeader("X-RateLimit-Remaining", String.valueOf(state.getRemaining()));
                response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                filterChain.doFilter(request, response);
            } else {
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIP, path);
                response.setStatus(429);
                response.setContentType("application/json");
                response.addHeader("Retry-After", "60");
                response.getWriter().write(
                        "{\"error\":\"Too many requests. Please wait and try again.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
            }
        }
    }
}
