package org.library.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private static final Logger logger = LoggerFactory.getLogger(SecurityProperties.class);

    private List<String> publicPaths;

    public SecurityProperties() {
        System.out.println("🚀 SecurityProperties bean created!");
    }

    @PostConstruct
    public void printPublicPaths() {
        System.out.println("🚀 SecurityProperties bean created!");
        System.out.println("✅ Public paths: " + publicPaths);
        logger.info("📢 Public Paths Configured: {}", publicPaths);
        
        // Debug: Print each path individually
        if (publicPaths != null) {
            logger.info("📢 Individual public paths:");
            for (int i = 0; i < publicPaths.size(); i++) {
                logger.info("📢   {}: '{}'", i, publicPaths.get(i));
            }
            
            // Check specifically for /dashboard
            boolean hasDashboard = publicPaths.contains("/dashboard");
            logger.info("📢 Contains /dashboard: {}", hasDashboard);
            
            if (hasDashboard) {
                logger.warn("⚠️  WARNING: /dashboard is in public paths! This will prevent JWT authentication.");
            }
        }
    }
}
