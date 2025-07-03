package org.library.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariDataSource;

import lombok.Setter;

@Setter
@Configuration
@Profile("!prod")  // Avoids using this configuration in production
public class DatabaseConfig { // Configuration class for database connection

    @Value("${spring.datasource.url}") // URL of the database
    private String url; // Database URL

    @Value("${spring.datasource.username}") // Username for the database
    private String username; // Database username

    @Value("${spring.datasource.password}") // Password for the database
    private String password; // Database password

    @Bean
    public DataSource dataSource() { // Bean for DataSource
        HikariDataSource dataSource = new HikariDataSource(); // HikariCP DataSource
        dataSource.setJdbcUrl(url); // Set the JDBC URL
        dataSource.setUsername(username); // Set the username
        dataSource.setPassword(password); // Set the password
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // MySQL driver class
        // Optional: Configure other Hikari settings if needed (e.g., connection pool size)
        dataSource.setMaximumPoolSize(10);  // Example: limit max connections to 10
        return dataSource; // Return the configured DataSource
    }

}