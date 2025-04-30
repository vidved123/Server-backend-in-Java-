package org.library.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private static final Logger logger = LoggerFactory.getLogger(SecurityProperties.class);
    private List<String> publicPaths;

    @PostConstruct
    public void printPublicPaths() {
        logger.info("📢 Public Paths Configured: {}", publicPaths);
    }
}
