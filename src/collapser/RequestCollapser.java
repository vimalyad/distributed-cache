package collapser;

import entities.key.CacheKey;
import entities.value.CacheValue;
import helpers.Loader;

public interface RequestCollapser<K, V> {
    CacheValue<V> getOrLoad(CacheKey<K> key, Loader<K, V> loader);
}
