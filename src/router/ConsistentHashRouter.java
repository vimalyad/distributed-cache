package router;

import entities.key.CacheKey;
import policies.distribution.DistributionPolicy;
import services.cache.CacheNode;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ConsistentHashRouter<K> implements DistributionPolicy<K> {
    private final TreeMap<Long, CacheNode<K, ?>> ring = new TreeMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final int virtualNodeCount;

    public ConsistentHashRouter(int virtualNodeCount) {
        this.virtualNodeCount = virtualNodeCount;
    }

    @Override
    public void addNode(CacheNode<K, ?> node) {
        lock.writeLock().lock();
        try {
            for (int v = 0; v < virtualNodeCount; v++) {
                long position = stableHashString(node.nodeId() + "#" + v);
                ring.put(position, node);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeNode(String nodeId) {
        lock.writeLock().lock();
        try {
            for (int v = 0; v < virtualNodeCount; v++) {
                long position = stableHashString(nodeId + "#" + v);
                ring.remove(position);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CacheNode<K, ?> route(CacheKey<K> key) {
        lock.readLock().lock();
        try {
            if (ring.isEmpty()) throw new IllegalStateException("No healthy nodes available.");
            long hash = key.getHash();
            Map.Entry<Long, CacheNode<K, ?>> entry = ring.higherEntry(hash);

            int attempts = 0;
            while (attempts < ring.size()) {
                if (entry == null) entry = ring.firstEntry();
                if (entry.getValue().isHealthy()) return entry.getValue();
                entry = ring.higherEntry(entry.getKey());
                attempts++;
            }
            throw new IllegalStateException("No healthy nodes available.");
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<CacheNode<K, ?>> allNodes() {
        lock.readLock().lock();
        try {
            return ring.values().stream().distinct().collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    private long stableHashString(String input) {
        long hash = 0xcbf29ce484222325L;
        for (byte b : input.getBytes()) {
            hash ^= b;
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
