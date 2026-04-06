package services.cache;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

import java.util.Optional;

public interface CacheNode<K, V> {
    String nodeId();

    Optional<CacheEntry<K, V>> get(CacheKey<K> key);

    void put(CacheEntry<K, V> entry);

    void delete(CacheKey<K> key);

    int size();

    int capacity();

    boolean isHealthy();
}