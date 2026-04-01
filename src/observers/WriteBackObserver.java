package observers;

import entities.event.CacheEvent;
import enums.CacheEventType;

public class WriteBackObserver<K, V> implements CacheObserver<K, V> {
    private final OriginStore<K, V> originStore;

    public WriteBackObserver(OriginStore<K, V> originStore) {
        this.originStore = originStore;
    }

    @Override
    public void onEvent(CacheEvent<K, V> event) {
        if (event.getType() == CacheEventType.ON_EVICTION && event.getValue() != null) {
            originStore.persist(event.getKey(), event.getValue());
        }
    }
}
