package client;

import entities.key.CacheKey;
import entities.value.CacheValue;

import java.util.Optional;

public interface CacheClient<K, V> {
    Optional<CacheValue<V>> get(CacheKey<K> key);

    void put(CacheKey<K> key, CacheValue<V> value, int ttlSeconds);

    void delete(CacheKey<K> key);
}