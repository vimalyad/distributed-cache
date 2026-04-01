package entities.entries;

import entities.key.CacheKey;
import entities.value.CacheValue;

public class DefaultCacheEntry<K, V> implements CacheEntry<K, V> {
    private final CacheKey<K> key;
    private final CacheValue<V> value;
    private final long createdAt;
    private final long expiresAt;

    public DefaultCacheEntry(CacheKey<K> key, CacheValue<V> value, long createdAt, long expiresAt) {
        this.key = key;
        this.value = value;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @Override
    public CacheKey<K> key() {
        return key;
    }

    @Override
    public CacheValue<V> value() {
        return value;
    }

    @Override
    public long createdAt() {
        return createdAt;
    }

    @Override
    public long expiredAt() {
        return expiresAt;
    }

    @Override
    public boolean isExpired(long currentTime) {
        return currentTime >= expiresAt;
    }
}

