package store;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

import java.util.Collection;
import java.util.Optional;

public interface CacheStore<K, V> {
    Optional<CacheEntry<K, V>> get(CacheKey<K> key);

    void put(CacheEntry<K, V> entry);

    void remove(CacheKey<K> key);

    int size();

    Collection<CacheKey<K>> keys();
}