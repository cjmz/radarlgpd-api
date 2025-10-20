package com.br.radarlgpd.radarlgpd.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para health check e informações básicas da API.
 */
@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Radar LGPD API");
        health.put("version", "0.0.1-SNAPSHOT");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "Radar LGPD API");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("status", "running");
        info.put("message", "API está funcionando! Use POST /v1/telemetry/scan-result para enviar dados.");
        
        return ResponseEntity.ok(info);
    }
}
