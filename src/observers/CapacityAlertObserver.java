package observers;

import entities.event.CacheEvent;
import enums.CacheEventType;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CapacityAlertObserver<K, V> implements CacheObserver<K, V> {
    private final AlertService alertService;
    private final int threshold;
    private final Queue<Long> evictionTimestamps = new ConcurrentLinkedQueue<>();

    public CapacityAlertObserver(AlertService alertService, int thresholdPerMinute) {
        this.alertService = alertService;
        this.threshold = thresholdPerMinute;
    }

    @Override
    public void onEvent(CacheEvent<K, V> event) {
        if (event.getType() == CacheEventType.ON_EVICTION) {
            long now = System.currentTimeMillis();
            evictionTimestamps.add(now);

            while (!evictionTimestamps.isEmpty() && now - evictionTimestamps.peek() > 60000) {
                evictionTimestamps.poll();
            }
            if (evictionTimestamps.size() > threshold) {
                alertService.trigger("High eviction rate detected! Count: " + evictionTimestamps.size() + " in last 60s.");
            }
        }
    }
}
