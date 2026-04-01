package entities.entries;

import entities.key.CacheKey;
import entities.value.CacheValue;

public interface CacheEntry<K, V> {
    CacheKey<K> key();

    CacheValue<V> value();

    long createdAt();

    long expiredAt();

    boolean isExpired(long currentTime);
}
