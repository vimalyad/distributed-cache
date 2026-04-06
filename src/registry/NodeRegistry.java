package registry;

import services.cache.CacheNode;

import java.util.List;
import java.util.Optional;

public interface NodeRegistry<K> {
    void register(CacheNode<K, ?> node);

    void deregister(String nodeId);

    List<CacheNode<K, ?>> activeNodes();

    Optional<CacheNode<K, ?>> findById(String nodeId);
}
