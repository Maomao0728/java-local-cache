package com.example.highconcurrencycache.service;

import com.example.highconcurrencycache.model.UserData;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockDatabaseService {

    private final Map<Long, UserData> mockTable = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        mockTable.put(1L, new UserData(1L, "Alice", "alice@example.com"));
        mockTable.put(2L, new UserData(2L, "Bob", "bob@example.com"));
        mockTable.put(3L, new UserData(3L, "Carol", "carol@example.com"));
    }

    public UserData getUserById(Long userId) {
        simulateDatabaseLatency();
        return mockTable.get(userId);
    }

    private void simulateDatabaseLatency() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Database query was interrupted", e);
        }
    }
}
