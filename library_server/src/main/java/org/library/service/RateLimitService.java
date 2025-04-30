package org.library.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final RateLimiter rateLimiter;

    public RateLimitService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public boolean tryConsume() {
        // Attempt to acquire a permission from the rate limiter
        return rateLimiter.acquirePermission();
    }
}
