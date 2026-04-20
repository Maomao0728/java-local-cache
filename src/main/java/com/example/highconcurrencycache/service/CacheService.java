package com.example.highconcurrencycache.service;

import com.example.highconcurrencycache.cache.CacheEntry;
import com.example.highconcurrencycache.cache.LocalCache;
import com.example.highconcurrencycache.model.UserData;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheService {

    private static final long USER_CACHE_TTL_MILLIS = 30_000L;
    private static final long NULL_CACHE_TTL_MILLIS = 5_000L;

    private final LocalCache localCache;
    private final MockDatabaseService mockDatabaseService;
    private final ConcurrentMap<String, Object> keyLocks = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong dbFallbackCount = new AtomicLong();

    public CacheService(LocalCache localCache, MockDatabaseService mockDatabaseService) {
        this.localCache = localCache;
        this.mockDatabaseService = mockDatabaseService;
    }

    public UserData getUserById(Long userId) {
        String cacheKey = buildUserKey(userId);
        CacheEntry cachedEntry = localCache.getEntry(cacheKey);
        if (cachedEntry != null) {
            hitCount.incrementAndGet();
            return cachedEntry.isNullValue() ? null : (UserData) cachedEntry.getValue();
        }

        missCount.incrementAndGet();
        Object lock = keyLocks.computeIfAbsent(cacheKey, key -> new Object());
        synchronized (lock) {
            try {
                CacheEntry doubleCheckedEntry = localCache.getEntry(cacheKey);
                if (doubleCheckedEntry != null) {
                    hitCount.incrementAndGet();
                    return doubleCheckedEntry.isNullValue() ? null : (UserData) doubleCheckedEntry.getValue();
                }

                dbFallbackCount.incrementAndGet();
                UserData userData = mockDatabaseService.getUserById(userId);
                if (userData == null) {
                    localCache.putNullValue(cacheKey, NULL_CACHE_TTL_MILLIS);
                    return null;
                }

                localCache.put(cacheKey, userData, USER_CACHE_TTL_MILLIS);
                return userData;
            } finally {
                keyLocks.remove(cacheKey, lock);
            }
        }
    }

    public void putUser(UserData userData, long ttlMillis) {
        localCache.put(buildUserKey(userData.getId()), userData, ttlMillis);
    }

    public void deleteUser(Long userId) {
        localCache.delete(buildUserKey(userId));
    }

    public int cacheSize() {
        return localCache.size();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        long hits = hitCount.get();
        long misses = missCount.get();
        long totalLookups = hits + misses;

        stats.put("cacheSize", localCache.size());
        stats.put("hitCount", hits);
        stats.put("missCount", misses);
        stats.put("dbFallbackCount", dbFallbackCount.get());
        stats.put("hitRate", totalLookups == 0 ? 0.0 : (double) hits / totalLookups);
        return stats;
    }

    private String buildUserKey(Long userId) {
        return "user:" + userId;
    }
}
