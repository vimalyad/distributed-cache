package store;

import entities.key.CacheKey;
import entities.value.CacheValue;

public interface OriginStore<K, V> {
    CacheValue<V> load(CacheKey<K> key);

    void persist(CacheKey<K> key, CacheValue<V> value);
}
