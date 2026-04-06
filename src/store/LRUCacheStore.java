package store;

import entities.entries.CacheEntry;
import entities.key.CacheKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LRUCacheStore<K, V> implements CacheStore<K, V> {
    private static class Node<K, V> {
        CacheEntry<K, V> entry;
        Node<K, V> prev, next;

        Node(CacheEntry<K, V> entry) {
            this.entry = entry;
        }
    }

    private final Map<CacheKey<K>, Node<K, V>> map = new HashMap<>();
    private final Node<K, V> head = new Node<>(null);
    private final Node<K, V> tail = new Node<>(null);

    public LRUCacheStore() {
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public Optional<CacheEntry<K, V>> get(CacheKey<K> key) {
        Node<K, V> node = map.get(key);
        if (node == null) return Optional.empty();
        moveToHead(node);
        return Optional.of(node.entry);
    }

    @Override
    public void put(CacheEntry<K, V> entry) {
        Node<K, V> node = map.get(entry.key());
        if (node != null) {
            node.entry = entry;
            moveToHead(node);
        } else {
            Node<K, V> newNode = new Node<>(entry);
            map.put(entry.key(), newNode);
            addToHead(newNode);
        }
    }

    @Override
    public void remove(CacheKey<K> key) {
        Node<K, V> node = map.remove(key);
        if (node != null) removeNode(node);
    }

    @Override
    public int size() {
        return map.size();
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private void addToHead(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
