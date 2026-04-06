package entities.key;

public interface CacheKey<K> {
    long getHash();

    String display();
}
