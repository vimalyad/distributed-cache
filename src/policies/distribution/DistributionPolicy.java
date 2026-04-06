package policies.distribution;

import entities.key.CacheKey;
import services.cache.CacheNode;

import java.util.List;

public interface DistributionPolicy<K> {
    CacheNode<K, ?> route(CacheKey<K> key);

    void addNode(CacheNode<K, ?> node);
}