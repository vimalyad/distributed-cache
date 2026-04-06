package services.ttl;

import entities.key.CacheKey;

public interface TTLManager<K> {
    void schedule(CacheKey<K> key, long expiresAtMs);

    void cancel(CacheKey<K> key);

    boolean isExpired(CacheKey<K> key, long nowMs);
}
