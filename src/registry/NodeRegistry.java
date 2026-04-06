package registry;

import services.cache.CacheNode;

import java.util.List;
import java.util.Optional;

public interface NodeRegistry<K> {
    void register(CacheNode<K, ?> node);
}
