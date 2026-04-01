package observers;

import entities.event.CacheEvent;
import enums.CacheEventType;

import java.util.concurrent.atomic.AtomicLong;

public class MetricsObserver<K, V> implements CacheObserver<K, V> {
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    @Override
    public void onEvent(CacheEvent<K, V> event) {
        if (event.getType() == CacheEventType.ON_HIT) hitCount.incrementAndGet();
        else if (event.getType() == CacheEventType.ON_MISS) missCount.incrementAndGet();
    }

    public double hitRate() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total == 0 ? 0.0 : (double) hits / total;
    }
}
