package policies.prefetch;

import entities.key.CacheKey;
import services.cache.CacheNode;

import java.util.Collections;
import java.util.List;

public class NoPrefetch<K, V> implements PrefetchPolicy<K, V> {
    @Override
    public List<CacheKey<K>> keysToPreload(CacheKey<K> accessedKey, CacheNode<K, V> node) {
        return Collections.emptyList();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
