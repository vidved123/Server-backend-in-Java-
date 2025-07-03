package org.library.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String imageFolder;
    private Security security;

    @Setter
    @Getter
    public static class Security {
        private List<String> publicPaths;

    }

}
