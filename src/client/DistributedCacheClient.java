package client;

import collapser.RequestCollapser;
import entities.entries.CacheEntry;
import entities.entries.DefaultCacheEntry;
import entities.key.CacheKey;
import entities.value.CacheValue;
import policies.distribution.DistributionPolicy;
import policies.prefetch.PrefetchLoader;
import policies.prefetch.PrefetchPolicy;
import publishers.CacheEventPublisher;
import services.cache.CacheNode;
import store.OriginStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class DistributedCacheClient<K, V> implements CacheClient<K, V> {
    private final DistributionPolicy<K> distributionPolicy;
    private final RequestCollapser<K, V> requestCollapser;
    private final PrefetchPolicy<K, V> prefetchPolicy;
    private final PrefetchLoader<K, V> prefetchLoader;
    private final OriginStore<K, V> originStore;
    private final CacheEventPublisher<K, V> publisher;
    private final ExecutorService prefetchExecutor;

    public DistributedCacheClient(DistributionPolicy<K> distributionPolicy, RequestCollapser<K, V> requestCollapser,
                                  PrefetchPolicy<K, V> prefetchPolicy, PrefetchLoader<K, V> prefetchLoader,
                                  OriginStore<K, V> originStore, CacheEventPublisher<K, V> publisher,
                                  ExecutorService prefetchExecutor) {
        this.distributionPolicy = distributionPolicy;
        this.requestCollapser = requestCollapser;
        this.prefetchPolicy = prefetchPolicy;
        this.prefetchLoader = prefetchLoader;
        this.originStore = originStore;
        this.publisher = publisher;
        this.prefetchExecutor = prefetchExecutor;
    }

    @Override
    public Optional<CacheValue<V>> get(CacheKey<K> key) {
        CacheNode<K, V> node = (CacheNode<K, V>) distributionPolicy.route(key);
        Optional<CacheEntry<K, V>> entryOpt = node.get(key);

        if (entryOpt.isPresent()) {
            if (prefetchPolicy.isEnabled()) {
                List<CacheKey<K>> keys = prefetchPolicy.keysToPreload(key, node);
                if (!keys.isEmpty()) {
                    prefetchExecutor.submit(() -> {
                        Map<CacheKey<K>, CacheValue<V>> batch = prefetchLoader.loadBatch(keys);
                        for (Map.Entry<CacheKey<K>, CacheValue<V>> bEntry : batch.entrySet()) {
                            CacheNode<K, V> targetNode = (CacheNode<K, V>) distributionPolicy.route(bEntry.getKey());
                            targetNode.put(new DefaultCacheEntry<>(bEntry.getKey(), bEntry.getValue(), System.currentTimeMillis(), Long.MAX_VALUE));
                        }
                    });
                }
            }
            return Optional.of(entryOpt.get().value());
        }

        CacheValue<V> value = requestCollapser.getOrLoad(key, originStore::load);
        if (value != null) {
            node.put(new DefaultCacheEntry<>(key, value, System.currentTimeMillis(), Long.MAX_VALUE));
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void put(CacheKey<K> key, CacheValue<V> value, int ttlSeconds) {
        CacheNode<K, V> node = (CacheNode<K, V>) distributionPolicy.route(key);
        long expiresAtMs = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000L : Long.MAX_VALUE;
        CacheEntry<K, V> entry = new DefaultCacheEntry<>(key, value, System.currentTimeMillis(), expiresAtMs);
        node.put(entry);
    }

    @Override
    public void delete(CacheKey<K> key) {
        CacheNode<K, V> node = (CacheNode<K, V>) distributionPolicy.route(key);
        node.delete(key);
    }
}
