package observers;

import utils.Logger;
import entities.event.CacheEvent;
import enums.CacheEventType;

public class EvictionLogger<K, V> implements CacheObserver<K, V> {
    private final Logger logger;

    public EvictionLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onEvent(CacheEvent<K, V> event) {
        if (event.getType() == CacheEventType.ON_EVICTION || event.getType() == CacheEventType.ON_EXPIRY) {
            logger.info("Event: " + event.getType() + " for key: " + event.getKey().display() + " at node: " + event.getNodeId() + " at " + event.getTimestamp());
        }
    }
}
