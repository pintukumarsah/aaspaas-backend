package com.aaspaas.aaspaas_backend.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestCorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RequestCorrelationIdFilter.class);

    private static final String HEADER_NAME = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    /*
     * Accept only short, plain identifiers.
     * Invalid or excessively long values are replaced with a UUID.
     */
    private static final Pattern SAFE_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{1,80}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(
                request.getHeader(HEADER_NAME)
        );

        long startTimeNanos = System.nanoTime();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs =
                    (System.nanoTime() - startTimeNanos) / 1_000_000;

            /*
             * Log method, path, status and duration only.
             * Do not log Authorization headers, request bodies,
             * passwords, payment data or query parameters.
             */
            log.info(
                    "http_request_completed method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );

            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(String incomingId) {

        if (incomingId != null
                && SAFE_ID_PATTERN.matcher(incomingId).matches()) {
            return incomingId;
        }

        return UUID.randomUUID().toString();
    }
}