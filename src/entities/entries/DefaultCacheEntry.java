package entities.entries;

import entities.key.CacheKey;
import entities.value.CacheValue;

public class DefaultCacheEntry<K, V> implements CacheEntry<K, V> {
    private final CacheKey<K> key;
    private final CacheValue<V> value;
    private final long expiresAt;

    public DefaultCacheEntry(CacheKey<K> key, CacheValue<V> value, long expiresAt) {
        this.key = key;
        this.value = value;
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
    public long expiredAt() {
        return expiresAt;
    }
}

