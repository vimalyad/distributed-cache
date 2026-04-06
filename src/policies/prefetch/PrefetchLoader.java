package policies.prefetch;

import entities.key.CacheKey;
import entities.value.CacheValue;

import java.util.List;
import java.util.Map;

public interface PrefetchLoader<K, V> {
    Map<CacheKey<K>, CacheValue<V>> loadBatch(List<CacheKey<K>> keys);
}
