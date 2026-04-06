package helpers;

import entities.key.CacheKey;

import java.util.List;

public interface KeySequencer<K> {
    List<CacheKey<K>> nextKeys(CacheKey<K> current, int count);
}
