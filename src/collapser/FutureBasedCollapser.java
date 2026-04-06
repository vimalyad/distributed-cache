package collapser;

import entities.key.CacheKey;
import entities.value.CacheValue;
import helpers.Loader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FutureBasedCollapser<K, V> implements RequestCollapser<K, V> {
    private final ConcurrentHashMap<CacheKey<K>, CompletableFuture<CacheValue<V>>> inFlight = new ConcurrentHashMap<>();

    @Override
    public CacheValue<V> getOrLoad(CacheKey<K> key, Loader<K, V> loader) {
        boolean[] isCreator = new boolean[1];
        CompletableFuture<CacheValue<V>> future = inFlight.computeIfAbsent(key, k -> {
            isCreator[0] = true;
            return new CompletableFuture<>();
        });

        if (isCreator[0]) {
            try {
                CacheValue<V> value = loader.load(key);
                future.complete(value);
                return value;
            } catch (Exception e) {
                future.completeExceptionally(e);
                throw e;
            } finally {
                inFlight.remove(key);
            }
        } else {
            try {
                return future.get(5000, TimeUnit.MILLISECONDS); // 5s timeout
            } catch (Exception e) {
                throw new RuntimeException("Fetch collapsed wait failed", e);
            }
        }
    }
}
