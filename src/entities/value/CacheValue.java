package entities.value;

public interface CacheValue<V> {
    V getValue();

    long sizeBytes();
}
