package registry;

import policies.distribution.DistributionPolicy;
import services.cache.CacheNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryNodeRegistry<K> implements NodeRegistry<K> {
    private final ConcurrentHashMap<String, CacheNode<K, ?>> nodes = new ConcurrentHashMap<>();
    private final DistributionPolicy<K> distributionPolicy;

    public InMemoryNodeRegistry(DistributionPolicy<K> distributionPolicy) {
        this.distributionPolicy = distributionPolicy;
    }

    @Override
    public void register(CacheNode<K, ?> node) {
        nodes.put(node.nodeId(), node);
        distributionPolicy.addNode(node);
    }
}
