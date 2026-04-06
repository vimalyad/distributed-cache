package helpers;

import entities.key.CacheKey;
import entities.value.CacheValue;

public interface Loader<K, V> {
    CacheValue<V> load(CacheKey<K> key);
}
