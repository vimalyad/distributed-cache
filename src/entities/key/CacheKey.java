package entities.key;

public interface CacheKey<K> {
    K rawKey();

    long getHash();

    String display();
}
