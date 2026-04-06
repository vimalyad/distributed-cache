package policies.prefetch;

import entities.key.CacheKey;
import helpers.KeySequencer;
import services.cache.CacheNode;

import java.util.List;
import java.util.stream.Collectors;

public class SequentialPrefetcher<K, V> implements PrefetchPolicy<K, V> {
    private final KeySequencer<K> sequencer;
    private final int prefetchCount;

    public SequentialPrefetcher(KeySequencer<K> sequencer, int prefetchCount) {
        this.sequencer = sequencer;
        this.prefetchCount = prefetchCount;
    }

    @Override
    public List<CacheKey<K>> keysToPreload(CacheKey<K> accessedKey, CacheNode<K, V> node) {
        List<CacheKey<K>> candidates = sequencer.nextKeys(accessedKey, prefetchCount);
        return candidates.stream()
                .filter(k -> node.get(k).isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
