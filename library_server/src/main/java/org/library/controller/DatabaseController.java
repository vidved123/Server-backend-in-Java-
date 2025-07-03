package org.library.controller;

import lombok.RequiredArgsConstructor;
import org.library.service.DatabaseInitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
@EnableWebSecurity
public class DatabaseController {

    private final DatabaseInitService databaseInitService;

    // You can secure this with Spring Security if needed, e.g.:
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/init")
    public ResponseEntity<String> initializeDatabase() {
        try {
            databaseInitService.initDatabase();
            return ResponseEntity.ok("✅ Database initialized.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to initialize database: " + e.getMessage());
        }
    }
}