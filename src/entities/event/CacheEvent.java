package entities.event;

import entities.key.CacheKey;
import entities.value.CacheValue;
import enums.CacheEventType;

public interface CacheEvent<K, V> {
    CacheEventType getType();

    CacheKey<K> getKey();

    CacheValue<V> getValue();

    String getNodeId();

    long getTimestamp();
}
