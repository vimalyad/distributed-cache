package observers;

import entities.event.CacheEvent;

public interface CacheObserver<K, V> {
    void onEvent(CacheEvent<K, V> event);
}
