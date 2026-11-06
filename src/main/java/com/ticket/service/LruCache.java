package com.ticket.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LruCache<K, V> {
    private final int capacity;
    private final Map<K, V> internalMap;

    public LruCache(int capacity) {
        this.capacity = capacity;
        // LinkedHashMap with accessOrder=true enables tracking based on recent lookups
        this.internalMap = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LruCache.this.capacity; // Evicts oldest element when map overflows capacity metrics
            }
        };
    }

    public synchronized void put(K key, V value) {
        internalMap.put(key, value);
    }

    public synchronized Optional<V> get(K key) {
        return Optional.ofNullable(internalMap.get(key));
    }

    public synchronized int size() {
        return internalMap.size();
    }

    public synchronized void clear() {
        internalMap.clear();
    }
}
