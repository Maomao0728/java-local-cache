package com.example.highconcurrencycache.cache;

public class CacheEntry {

    private final Object value;
    private final long expireAt;
    private final boolean nullValue;

    public CacheEntry(Object value, long expireAt, boolean nullValue) {
        this.value = value;
        this.expireAt = expireAt;
        this.nullValue = nullValue;
    }

    public static CacheEntry of(Object value, long ttlMillis) {
        return new CacheEntry(value, calculateExpireAt(ttlMillis), false);
    }

    public static CacheEntry nullEntry(long ttlMillis) {
        return new CacheEntry(null, calculateExpireAt(ttlMillis), true);
    }

    public Object getValue() {
        return value;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public boolean isNullValue() {
        return nullValue;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireAt;
    }

    private static long calculateExpireAt(long ttlMillis) {
        return System.currentTimeMillis() + ttlMillis;
    }
}
