package com.example.highconcurrencycache.controller;

import com.example.highconcurrencycache.dto.CachePutRequest;
import com.example.highconcurrencycache.model.UserData;
import com.example.highconcurrencycache.service.CacheService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserData> getUser(@PathVariable Long id) {
        UserData userData = cacheService.getUserById(id);
        if (userData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userData);
    }

    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> putUser(@Valid @RequestBody CachePutRequest request) {
        UserData userData = new UserData(request.getId(), request.getName(), request.getEmail());
        cacheService.putUser(userData, request.getTtlMillis());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User cached successfully");
        response.put("cacheSize", cacheService.cacheSize());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        cacheService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User cache deleted successfully");
        response.put("cacheSize", cacheService.cacheSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(cacheService.getStats());
    }
}
