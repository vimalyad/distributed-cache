package entities.value;

import java.io.Serializable;

public class SerializableCacheValue<V extends Serializable> implements CacheValue<V> {
    private final V value;
    private final long estimatedSizeBytes;

    public SerializableCacheValue(V value, long estimatedSizeBytes) {
        this.value = value;
        this.estimatedSizeBytes = estimatedSizeBytes;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public long sizeBytes() {
        return estimatedSizeBytes;
    }
}
