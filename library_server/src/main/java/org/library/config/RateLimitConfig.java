package org.library.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limiter.limit-for-period:10}") // Default: 10 requests
    private int limitForPeriod;

    @Value("${rate-limiter.limit-refresh-period:60}") // Default: 60 seconds
    private int limitRefreshPeriod;

    @Value("${rate-limiter.timeout-duration:500}") // Default: 500 ms
    private int timeoutDuration;

    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.of("rateLimiter",
                RateLimiterConfig.custom()
                        .limitForPeriod(limitForPeriod)
                        .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriod))
                        .timeoutDuration(Duration.ofMillis(timeoutDuration))
                        .build()
        );
    }
}
