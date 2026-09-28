package com.militaryasset.module.health;

import com.militaryasset.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "UP");
        details.put("service", "Military Asset Management System API");
        details.put("timestamp", LocalDateTime.now().toString());

        boolean dbConnected = false;
        try (Connection conn = dataSource.getConnection()) {
            dbConnected = conn.isValid(2);
            details.put("database", dbConnected ? "CONNECTED" : "DISCONNECTED");
            details.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            details.put("database", "ERROR: " + e.getMessage());
        }

        if (dbConnected) {
            return ResponseEntity.ok(ApiResponse.success("System is healthy and database is connected", details));
        } else {
            return ResponseEntity.status(503).body(ApiResponse.error("Database connection failure"));
        }
    }
}
