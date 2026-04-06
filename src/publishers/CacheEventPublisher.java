package publishers;

import entities.event.CacheEvent;
import observers.CacheObserver;

public interface CacheEventPublisher<K, V> {
    void register(CacheObserver<K, V> observer);

    void publish(CacheEvent<K, V> event);
}
