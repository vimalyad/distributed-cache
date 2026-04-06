package policies.prefetch;

import entities.key.CacheKey;
import services.cache.CacheNode;

import java.util.List;

public interface PrefetchPolicy<K, V> {
    List<CacheKey<K>> keysToPreload(CacheKey<K> accessedKey, CacheNode<K, V> node);

    boolean isEnabled();
}
