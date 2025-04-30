package org.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

@Configuration
@Profile("!prod")  // Avoids using this configuration in production
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // MySQL driver class
        // Optional: Configure other Hikari settings if needed (e.g., connection pool size)
        dataSource.setMaximumPoolSize(10);  // Example: limit max connections to 10
        return dataSource;
    }
}
