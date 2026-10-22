package com.example.food_delivery_app.config;

import com.example.food_delivery_app.common.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;                       // NOTE: must be slf4j, not org.jboss.logging
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


/**
 * Gives every incoming HTTP request a unique "correlation id" so that all log lines
 * written while handling that request can be grouped together.
 *
 * How it works:
 *  1. Read the id from the "X-Correlation-Id" request header (if a client/gateway sent one),
 *     otherwise generate a new random UUID.
 *  2. Put the id in the MDC (a per-thread map used by the logger). The logging pattern
 *     in application.yml prints it on every line using %X{correlationId}.
 *  3. Echo the id back in the response header so the client can report it to us.
 *  4. Always remove the id when the request finishes.
 *
 * Runs once per request (OncePerRequestFilter) and before every other filter
 * (HIGHEST_PRECEDENCE), so even Spring Security logs carry the id.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    // Name of the HTTP header used to receive and return the id.
    public static final String HEADER = Constants.HEADER_CORRELATION_ID;

    // Key used inside the MDC. Must match %X{correlationId} in the logging pattern.
    public static final String MDC_KEY = Constants.MDC_KEY;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. Reuse the id sent by the caller, if any.
        String id = request.getHeader(HEADER);

        // The header comes from outside, so don't trust it blindly:
        // if it is missing, blank, or suspiciously long, generate our own.
        if (id == null || id.isBlank() || id.length() > 64) {
            id = UUID.randomUUID().toString();
        }

        // 2. Store it for this thread. Every log line from now on will include it.
        MDC.put(MDC_KEY, id);

        // 3. Send it back so the client (Postman, browser, frontend) can see it.
        response.setHeader(HEADER, id);

        long start = System.currentTimeMillis();
        try {
            // First log line of the request: tells us which endpoint this id belongs to.
            log.info("--> {} {}", request.getMethod(), request.getRequestURI());

            // Continue to the next filter / the controller.
            chain.doFilter(request, response);
        } finally {
            // Last log line of the request: status code and how long it took.
            log.info("<-- {} {} ({} ms)", response.getStatus(),
                    request.getRequestURI(), System.currentTimeMillis() - start);

            // 4. Threads are reused by the server. If we don't clear the id here,
            // the next request handled by this thread would show the OLD id in its logs.
            // finally guarantees this runs even when an exception is thrown.
            MDC.remove(MDC_KEY);
        }
    }
}