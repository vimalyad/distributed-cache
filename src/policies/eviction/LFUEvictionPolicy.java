package policies.eviction;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

public class LFUEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final Map<CacheKey<K>, Integer> frequencyMap = new HashMap<>();
    private final TreeMap<Integer, LinkedHashSet<CacheKey<K>>> buckets = new TreeMap<>();
    private int minFrequency = 0;

    @Override
    public void onAccess(CacheEntry<K, V> entry) {
        CacheKey<K> key = entry.key();
        int freq = frequencyMap.getOrDefault(key, 0);

        buckets.get(freq).remove(key);
        if (buckets.get(freq).isEmpty() && minFrequency == freq) {
            minFrequency++;
        }

        int newFreq = freq + 1;
        frequencyMap.put(key, newFreq);
        buckets.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    @Override
    public void onInsert(CacheEntry<K, V> entry) {
        CacheKey<K> key = entry.key();
        frequencyMap.put(key, 1);
        minFrequency = 1;
        buckets.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
    }

    @Override
    public void onRemove(CacheKey<K> key) {
        Integer freq = frequencyMap.remove(key);
        if (freq != null) {
            buckets.get(freq).remove(key);
        }
    }

    @Override
    public CacheKey<K> selectEvictionCandidate() {
        if (buckets.isEmpty() || !buckets.containsKey(minFrequency)) return null;
        LinkedHashSet<CacheKey<K>> candidates = buckets.get(minFrequency);
        if (candidates.isEmpty()) return null;
        return candidates.getFirst();
    }
}
