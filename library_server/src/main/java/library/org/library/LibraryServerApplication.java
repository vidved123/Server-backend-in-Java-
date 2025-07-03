package org.library;

import org.library.config.AppProperties;
import org.library.config.JwtProperties;
import org.library.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterProperties;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.library.repository")
@EntityScan(basePackages = "org.library.entity")
@EnableConfigurationProperties({
        JwtProperties.class,
        AppProperties.class,
        RateLimiterProperties.class,
        SecurityProperties.class
})
public class LibraryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryServerApplication.class, args);
    }
}
