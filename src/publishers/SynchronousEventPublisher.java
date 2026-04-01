package publishers;

import entities.event.CacheEvent;
import observers.CacheObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// the publishers will be following default synchronous nature
public class SynchronousEventPublisher<K, V> implements CacheEventPublisher<K, V> {
    // thread safety for adding and removing listeners
    private final List<CacheObserver<K, V>> observers = new CopyOnWriteArrayList<>();

    @Override
    public void register(CacheObserver<K, V> observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(CacheObserver<K, V> observer) {
        observers.remove(observer);
    }

    @Override
    public void publish(CacheEvent<K, V> event) {
        for (CacheObserver<K, V> observer : observers) {
            observer.onEvent(event);
        }
    }
}
