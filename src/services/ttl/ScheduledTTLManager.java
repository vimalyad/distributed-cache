package services.ttl;

import entities.key.CacheKey;
import services.cache.CacheNode;

import java.util.concurrent.*;

public class ScheduledTTLManager<K> implements TTLManager<K> {
    private final ConcurrentHashMap<CacheKey<K>, Long> expiryMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CacheKey<K>, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private CacheNode<K, ?> node; // Lazy injection or setter to avoid circular dependency

    public void setNode(CacheNode<K, ?> node) {
        this.node = node;
    }

    @Override
    public void schedule(CacheKey<K> key, long expiresAtMs) {
        if (expiresAtMs == Long.MAX_VALUE) return;

        expiryMap.put(key, expiresAtMs);
        long delay = expiresAtMs - System.currentTimeMillis();

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            if (node != null) node.delete(key);
        }, delay > 0 ? delay : 0, TimeUnit.MILLISECONDS);

        futures.put(key, future);
    }

    @Override
    public void cancel(CacheKey<K> key) {
        expiryMap.remove(key);
        ScheduledFuture<?> future = futures.remove(key);
        if (future != null) future.cancel(false);
    }

    @Override
    public boolean isExpired(CacheKey<K> key, long nowMs) {
        Long expiresAt = expiryMap.get(key);
        return expiresAt != null && nowMs >= expiresAt;
    }
}
