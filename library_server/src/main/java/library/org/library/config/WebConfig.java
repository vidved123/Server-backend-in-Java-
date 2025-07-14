package org.library.config;

import org.library.filter.RateLimitFilter;
import org.library.service.RateLimitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
            .addResourceLocations(
                "file:/Users/vidhyut/Desktop/Prathisthan Projects/Server-Backend-java/library-images/",
                "classpath:/static/images/"
            );
    }

    @Bean
    public RateLimitFilter rateLimitingFilter(RateLimitService rateLimitService) {
        return new RateLimitFilter(rateLimitService);
    }
}
