package factory;

import client.CacheClient;
import client.DistributedCacheClient;
import utils.Logger;
import store.OriginStore;
import collapser.FutureBasedCollapser;
import collapser.RequestCollapser;
import observers.CapacityAlertObserver;
import observers.EvictionLogger;
import observers.MetricsObserver;
import observers.WriteBackObserver;
import policies.distribution.DistributionPolicy;
import policies.eviction.EvictionPolicy;
import policies.eviction.LRUEvictionPolicy;
import policies.prefetch.NoPrefetch;
import policies.prefetch.PrefetchLoader;
import policies.prefetch.PrefetchPolicy;
import publishers.SynchronousEventPublisher;
import registry.InMemoryNodeRegistry;
import registry.NodeRegistry;
import router.ConsistentHashRouter;
import services.alert.AlertService;
import services.cache.LocalCacheNode;
import services.ttl.ScheduledTTLManager;
import store.CacheStore;
import store.LRUCacheStore;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheSystemFactory {

    public static <K, V> CacheClient<K, V> buildDefault(int nodeCount, int capacityPerNode, int virtualNodesPerPhysical,
                                                 int defaultTtlSeconds, OriginStore<K, V> originStore,
                                                 Logger logger, AlertService alertService) {

        DistributionPolicy<K> distributionPolicy = new ConsistentHashRouter<>(virtualNodesPerPhysical);
        NodeRegistry<K> nodeRegistry = new InMemoryNodeRegistry<>(distributionPolicy);
        RequestCollapser<K, V> collapser = new FutureBasedCollapser<>();
        PrefetchPolicy<K, V> prefetchPolicy = new NoPrefetch<>();

        PrefetchLoader<K, V> prefetchLoader = keys -> Collections.emptyMap();

        SynchronousEventPublisher<K, V> publisher = new SynchronousEventPublisher<>();
        publisher.register(new EvictionLogger<>(logger));
        publisher.register(new MetricsObserver<>());
        publisher.register(new WriteBackObserver<>(originStore));
        publisher.register(new CapacityAlertObserver<>(alertService, 100)); // 100 per minute threshold

        for (int i = 0; i < nodeCount; i++) {
            CacheStore<K, V> store = new LRUCacheStore<>();
            EvictionPolicy<K, V> evictionPolicy = new LRUEvictionPolicy<>();
            ScheduledTTLManager<K> ttlManager = new ScheduledTTLManager<>();

            LocalCacheNode<K, V> node = new LocalCacheNode<>(
                    "node-" + i, capacityPerNode, store, evictionPolicy, ttlManager, publisher
            );

            ttlManager.setNode(node);
            nodeRegistry.register(node);
        }

        ExecutorService prefetchExecutor = Executors.newFixedThreadPool(2);

        return new DistributedCacheClient<>(
                distributionPolicy, collapser, prefetchPolicy, prefetchLoader,
                originStore, publisher, prefetchExecutor
        );
    }

    private CacheSystemFactory(){}
}
