package policies.eviction;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

import java.util.HashMap;
import java.util.Map;

public class LRUEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private static class Node<K> {
        CacheKey<K> key;
        Node<K> prev, next;

        Node(CacheKey<K> key) {
            this.key = key;
        }
    }

    private final Map<CacheKey<K>, Node<K>> map = new HashMap<>();
    private final Node<K> head = new Node<>(null);
    private final Node<K> tail = new Node<>(null);

    public LRUEvictionPolicy() {
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void onAccess(CacheEntry<K, V> entry) {
        Node<K> node = map.get(entry.key());
        if (node != null) moveToHead(node);
    }

    @Override
    public void onInsert(CacheEntry<K, V> entry) {
        Node<K> newNode = new Node<>(entry.key());
        map.put(entry.key(), newNode);
        addToHead(newNode);
    }

    @Override
    public void onRemove(CacheKey<K> key) {
        Node<K> node = map.remove(key);
        if (node != null) removeNode(node);
    }

    @Override
    public CacheKey<K> selectEvictionCandidate() {
        if (tail.prev == head) return null;
        return tail.prev.key; 
    }

    private void moveToHead(Node<K> node) {
        removeNode(node);
        addToHead(node);
    }

    private void addToHead(Node<K> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
