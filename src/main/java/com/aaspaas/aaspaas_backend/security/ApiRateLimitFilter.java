package com.aaspaas.aaspaas_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(-90)
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER =
            "X-Correlation-ID";

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, Window> windows =
            new ConcurrentHashMap<>();

    private final AtomicLong requestCounter = new AtomicLong();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.login-path:/api/auth/login}")
    private String loginPath;

    @Value("${app.rate-limit.login-limit:10}")
    private int loginLimit;

    @Value("${app.rate-limit.login-window-seconds:60}")
    private long loginWindowSeconds;

    @Value("${app.rate-limit.public-limit:120}")
    private int publicLimit;

    @Value("${app.rate-limit.public-window-seconds:60}")
    private long publicWindowSeconds;

    @Value("${app.rate-limit.default-limit:180}")
    private int defaultLimit;

    @Value("${app.rate-limit.default-window-seconds:60}")
    private long defaultWindowSeconds;

    public ApiRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !enabled
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Policy policy = resolvePolicy(request);

        if (policy.limit() <= 0 || policy.windowSeconds() <= 0) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Invalid rate limit configuration"
            );
            return;
        }

        /*
         * Use the servlet container's remote address.
         * Do not trust client-supplied X-Forwarded-For by default.
         */
        String clientIp = request.getRemoteAddr();

        String bucketKey = clientIp + "|" + policy.name();

        long now = System.currentTimeMillis();
        long windowMillis = policy.windowSeconds() * 1000L;

        Decision decision = windows.compute(bucketKey, (key, current) -> {

            if (current == null
                    || now - current.windowStartMillis >= windowMillis) {

                return new Window(now, 1, 1);
            }

            int nextCount = current.requestCount + 1;

            return new Window(
                    current.windowStartMillis,
                    nextCount,
                    current.requestCount >= policy.limit()
                            ? 0
                            : 1
            );
        }).toDecision(policy.limit(), windowMillis, now);

        cleanupOccasionally(now);

        if (!decision.allowed()) {
            long retryAfterSeconds = Math.max(
                    1,
                    (decision.resetAtMillis() - now + 999) / 1000
            );

            // response.setStatus(
            //         HttpServletResponse.SC_TOO_MANY_REQUESTS
            // );
            // HttpServletResponse.SC_TOO_MANY_REQUESTS teesesi direct ga 429 pettandi:
response.setStatus(429);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            response.setHeader(
                    HttpHeaders.RETRY_AFTER,
                    String.valueOf(retryAfterSeconds)
            );

            response.setHeader(
                    "X-RateLimit-Limit",
                    String.valueOf(policy.limit())
            );

            response.setHeader(
                    "X-RateLimit-Remaining",
                    "0"
            );

            response.setHeader(
                    "X-RateLimit-Reset",
                    String.valueOf(
                            (decision.resetAtMillis() + 999) / 1000
                    )
            );

            String correlationId =
                    request.getHeader(CORRELATION_HEADER);

            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 429,
                    "error", "TOO_MANY_REQUESTS",
                    "message", "Too many requests. Please try again later.",
                    "path", request.getRequestURI(),
                    "correlationId",
                    correlationId == null ? "" : correlationId
            );

            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(policy.limit())
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(decision.remaining())
        );

        response.setHeader(
                "X-RateLimit-Reset",
                String.valueOf(
                        (decision.resetAtMillis() + 999) / 1000
                )
        );

        filterChain.doFilter(request, response);
    }

    private Policy resolvePolicy(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)
                && loginPath.equals(path)) {

            return new Policy(
                    "login",
                    loginLimit,
                    loginWindowSeconds
            );
        }

        boolean publicRead =
                "GET".equalsIgnoreCase(method)
                        && (path.startsWith("/api/products")
                        || path.startsWith("/api/categories"));

        if (publicRead) {
            return new Policy(
                    "public-read",
                    publicLimit,
                    publicWindowSeconds
            );
        }

        return new Policy(
                "default",
                defaultLimit,
                defaultWindowSeconds
        );
    }

    private void cleanupOccasionally(long now) {

        // Avoid scanning the map on every request.
        if (requestCounter.incrementAndGet() % 1000 != 0) {
            return;
        }

        long maximumAgeMillis = Math.max(
                loginWindowSeconds,
                Math.max(publicWindowSeconds, defaultWindowSeconds)
        ) * 1000L;

        windows.entrySet().removeIf(entry ->
                now - entry.getValue().windowStartMillis
                        > maximumAgeMillis * 2
        );
    }

    private record Policy(
            String name,
            int limit,
            long windowSeconds
    ) {
    }

    private record Decision(
            boolean allowed,
            int remaining,
            long resetAtMillis
    ) {
    }

    private record Window(
            long windowStartMillis,
            int requestCount,
            int lastRequestAllowed
    ) {
        Decision toDecision(
                int limit,
                long windowMillis,
                long now
        ) {
            long resetAt = windowStartMillis + windowMillis;

            boolean allowed = lastRequestAllowed == 1;

            int remaining = Math.max(0, limit - requestCount);

            return new Decision(allowed, remaining, resetAt);
        }
    }
}