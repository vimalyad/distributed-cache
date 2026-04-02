package policies.eviction;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

public interface EvictionPolicy<K, V> {
    void onAccess(CacheEntry<K, V> entry);

    void onInsert(CacheEntry<K, V> entry);

    void onRemove(CacheKey<K> key);

    CacheKey<K> selectEvictionCandidate();
}
