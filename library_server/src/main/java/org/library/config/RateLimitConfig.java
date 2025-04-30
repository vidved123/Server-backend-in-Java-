package org.library.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limiter.limit-for-period}")
    private int limitForPeriod;

    @Value("${rate-limiter.limit-refresh-period}")
    private int limitRefreshPeriod;

    @Value("${rate-limiter.timeout-duration}")
    private int timeoutDuration;

    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.of("rateLimiter",
                RateLimiterConfig.custom()
                        .limitForPeriod(limitForPeriod)  // Allow up to 'limitForPeriod' requests
                        .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriod))  // Every 'limitRefreshPeriod' seconds
                        .timeoutDuration(Duration.ofMillis(timeoutDuration))  // Wait max 'timeoutDuration' ms for a permit
                        .build()
        );
    }
}
