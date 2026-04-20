package com.example.highconcurrencycache.cache;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LocalCache {

    private static final int DEFAULT_MAX_CAPACITY = 100;

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, Boolean> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
    private final ReentrantLock lruLock = new ReentrantLock();
    private final int maxCapacity;

    public LocalCache() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public LocalCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void put(String key, Object value, long ttlMillis) {
        cache.put(key, CacheEntry.of(value, ttlMillis));
        recordAccess(key);
        evictIfNeeded();
    }

    public void putNullValue(String key, long ttlMillis) {
        cache.put(key, CacheEntry.nullEntry(ttlMillis));
        recordAccess(key);
        evictIfNeeded();
    }

    public CacheEntry getEntry(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            removeAccessRecord(key);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            removeAccessRecord(key);
            return null;
        }

        recordAccess(key);
        return entry;
    }

    public Object get(String key) {
        CacheEntry entry = getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    public boolean contains(String key) {
        return getEntry(key) != null;
    }

    public void delete(String key) {
        cache.remove(key);
        removeAccessRecord(key);
    }

    public int size() {
        return cache.size();
    }

    private void evictIfNeeded() {
        lruLock.lock();
        try {
            while (cache.size() > maxCapacity && !accessOrder.isEmpty()) {
                Iterator<Map.Entry<String, Boolean>> iterator = accessOrder.entrySet().iterator();
                if (!iterator.hasNext()) {
                    return;
                }

                String eldestKey = iterator.next().getKey();
                iterator.remove();
                cache.remove(eldestKey);
            }
        } finally {
            lruLock.unlock();
        }
    }

    private void recordAccess(String key) {
        lruLock.lock();
        try {
            accessOrder.put(key, Boolean.TRUE);
        } finally {
            lruLock.unlock();
        }
    }

    private void removeAccessRecord(String key) {
        lruLock.lock();
        try {
            accessOrder.remove(key);
        } finally {
            lruLock.unlock();
        }
    }
}
