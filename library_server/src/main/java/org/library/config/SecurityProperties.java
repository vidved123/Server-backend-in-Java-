package org.library.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

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
        System.out.println("✅ Public paths: " + publicPaths);
        logger.info("📢 Public Paths Configured: {}", publicPaths);
    }
}
