package com.example.contactmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter applied to /api/auth/login.
 *
 * <p>Limits each IP address to 5 login attempts per minute.
 * Exceeding the limit returns 429 Too Many Requests.</p>
 *
 * <p>Uses Bucket4j token bucket algorithm — each IP gets its own
 * bucket that refills 5 tokens every 60 seconds.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ErrorResponseWriter errorResponseWriter;

    public RateLimitFilter(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!("/api/auth/login".equals(request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

       String ip = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::createBucket);

        if (!bucket.tryConsume(1)) {
            errorResponseWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again in 1 minute.",
                    request.getRequestURI()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Creates a new bucket for an IP address.
     * Allows 5 requests per minute — refills every 60 seconds.
     */
    private Bucket createBucket(String ip) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Extracts the real client IP, accounting for reverse proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    /**
     * For testing — clears all rate limit buckets.
     */
    void clearBuckets() {
        buckets.clear();
    }
}
