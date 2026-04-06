package services.cache;

import entities.entries.CacheEntry;
import entities.event.DefaultCacheEvent;
import entities.key.CacheKey;
import enums.CacheEventType;
import policies.eviction.EvictionPolicy;
import publishers.CacheEventPublisher;
import services.ttl.TTLManager;
import store.CacheStore;

import java.util.Optional;

public class LocalCacheNode<K, V> implements CacheNode<K, V> {
    private final CacheStore<K, V> store;
    private final EvictionPolicy<K, V> evictionPolicy;
    private final TTLManager<K> ttlManager;
    private final CacheEventPublisher<K, V> publisher;
    private final int capacity;
    private final String nodeId;
    private volatile boolean healthy = true;

    public LocalCacheNode(String nodeId, int capacity, CacheStore<K, V> store,
                          EvictionPolicy<K, V> evictionPolicy, TTLManager<K> ttlManager,
                          CacheEventPublisher<K, V> publisher) {
        this.nodeId = nodeId;
        this.capacity = capacity;
        this.store = store;
        this.evictionPolicy = evictionPolicy;
        this.ttlManager = ttlManager;
        this.publisher = publisher;
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    @Override
    public synchronized Optional<CacheEntry<K, V>> get(CacheKey<K> key) {
        long now = System.currentTimeMillis();
        if (ttlManager.isExpired(key, now)) {
            delete(key);
            return Optional.empty();
        }

        Optional<CacheEntry<K, V>> entryOpt = store.get(key);
        if (!entryOpt.isPresent()) {
            publisher.publish(new DefaultCacheEvent<>(CacheEventType.ON_MISS, key, null, nodeId, now));
            return Optional.empty();
        }

        CacheEntry<K, V> entry = entryOpt.get();
        evictionPolicy.onAccess(entry);
        publisher.publish(new DefaultCacheEvent<>(CacheEventType.ON_HIT, key, entry.value(), nodeId, now));
        return entryOpt;
    }

    @Override
    public synchronized void put(CacheEntry<K, V> entry) {
        long now = System.currentTimeMillis();
        if (store.size() >= capacity && !store.get(entry.key()).isPresent()) {
            CacheKey<K> candidateKey = evictionPolicy.selectEvictionCandidate();
            if (candidateKey != null) {
                Optional<CacheEntry<K, V>> evictedOpt = store.get(candidateKey);
                store.remove(candidateKey);
                ttlManager.cancel(candidateKey);
                evictionPolicy.onRemove(candidateKey);

                evictedOpt.ifPresent(evicted ->
                        publisher.publish(new DefaultCacheEvent<>(CacheEventType.ON_EVICTION, candidateKey, evicted.value(), nodeId, now))
                );
            }
        }

        store.put(entry);
        ttlManager.schedule(entry.key(), entry.expiredAt());
        evictionPolicy.onInsert(entry);
        publisher.publish(new DefaultCacheEvent<>(CacheEventType.ON_PUT, entry.key(), entry.value(), nodeId, now));
    }

    @Override
    public synchronized void delete(CacheKey<K> key) {
        long now = System.currentTimeMillis();
        store.remove(key);
        ttlManager.cancel(key);
        evictionPolicy.onRemove(key);
        publisher.publish(new DefaultCacheEvent<>(CacheEventType.ON_DELETE, key, null, nodeId, now));
    }
}