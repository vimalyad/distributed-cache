package entities.event;

import entities.key.CacheKey;
import entities.value.CacheValue;
import enums.CacheEventType;

public class DefaultCacheEvent<K, V> implements CacheEvent<K, V> {
    private final CacheEventType type;
    private final CacheKey<K> key;
    private final CacheValue<V> value;
    private final String nodeId;
    private final long timestamp;

    public DefaultCacheEvent(CacheEventType type, CacheKey<K> key, CacheValue<V> value, String nodeId, long timestamp) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    @Override
    public CacheEventType getType() {
        return type;
    }

    @Override
    public CacheKey<K> getKey() {
        return key;
    }

    @Override
    public CacheValue<V> getValue() {
        return value;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
