package com.segment.redisproxy;

import java.util.*;

class CacheNode {
    public CacheNode prev;
    public CacheNode next;
    public final String key;
    public String value;
    public final long arrivalTimeMillis;

    public CacheNode(String key, String value) {
        this.key = key;
        this.value = value;
        this.arrivalTimeMillis = System.currentTimeMillis();
    }
}

public class RedisProxyCache {
    private HashMap<String, CacheNode> cache;
    private CacheNode recentlyUsedFront;
    private CacheNode recentlyUsedBack;
    private int capacity;
    private long globalExpiryMillis;

    public RedisProxyCache(int capacity, long globalExpiryMillis) throws IllegalArgumentException {
        if (capacity < 0) {
            throw new IllegalArgumentException("Cache capacity cannot be negative");
        }
        if (globalExpiryMillis < 0) {
            throw new IllegalArgumentException("Global expiry cannot be negative");
        }

        this.cache = new HashMap<>();
        this.recentlyUsedFront = null;
        this.recentlyUsedBack = null;
        this.capacity = capacity;
        this.globalExpiryMillis = globalExpiryMillis;
    }

    public void set(String key, String value) {
        CacheNode getResult = this.cache.get(key);

        // If already in the cache, just update
        if (getResult != null) {
            System.out.println("Already found in cache");
            getResult.value = value;
            moveToFront(getResult);
        }
        // If not already in the cache, we need to ensure we have space
        else {
            // If cache is full, see if we can clear stale entries first
            if (this.cache.size() == this.capacity) {
                clearStaleEntries();
            }
            // If still full, evict the least recently used item
            if (this.cache.size() == this.capacity) {
                evictLRU();
            }

            CacheNode node = new CacheNode(key, value);
            addToFront(node);
        }
    }

    public String get(String key) {
        CacheNode getResult = this.cache.get(key);
        System.out.println(this.cache);
        if (getResult != null) {
            // If key is in the cache but the entry is stale,
            // pretend it doesn't exist and get rid of it
            if (isStale(getResult)) {
                removeNode(getResult);
                return null;
            }
            else {
                moveToFront(getResult);
                return getResult.value;
            }
        }
        else {
            return null;
        }
    }

    public boolean containsValidEntry(String key) {
        CacheNode getResult = this.cache.get(key);
        return (getResult != null && !isStale(getResult));
    }

    private CacheNode removeNode(CacheNode node) {
        this.cache.remove(node.key);

        if (node == recentlyUsedFront && node == recentlyUsedBack) {
            recentlyUsedFront = null;
            recentlyUsedBack = null;
            node.prev = null;
            node.next = null;
            return node;
        }
        if (node == recentlyUsedFront) {
            recentlyUsedFront = node.next;
            recentlyUsedFront.prev = null;
            node.prev = null;
            node.next = null;
            return node;
        }
        if (node == recentlyUsedBack) {
            recentlyUsedBack = node.prev;
            recentlyUsedBack.next = null;
            node.prev = null;
            node.next = null;
            return node;
        }

        // System.out.println(node);
        // System.out.println(node.prev);
        // System.out.println(node.next);
        // System.out.println(recentlyUsedFront);
        // System.out.println(recentlyUsedBack);
        CacheNode oldPrev = node.prev;
        CacheNode oldNext = node.next;
        oldPrev.next = oldNext;
        oldNext.prev = oldPrev;
        node.prev = null;
        node.next = null;
        return node;
    }

    private void addToFront(CacheNode node) {
        this.cache.put(node.key, node);

        if (recentlyUsedFront == null && recentlyUsedBack == null) {
            recentlyUsedFront = node;
            recentlyUsedBack = node;
            node.prev = null;
            node.next = null;
            return;
        }
        recentlyUsedFront.prev = node;
        node.next = recentlyUsedFront;
        node.prev = null;
        recentlyUsedFront = node;
    }

    private void moveToFront(CacheNode node) {
        addToFront(removeNode(node));
    }

    private void evictLRU() {
        removeNode(recentlyUsedBack);
        // this.cache.remove(recentlyUsedBack.key);

        // if (recentlyUsedBack == null) {
        //     return;
        // }
        // if (recentlyUsedFront == recentlyUsedBack) {
        //     recentlyUsedFront = null;
        //     recentlyUsedBack = null;
        //     return;
        // }
        // recentlyUsedBack = recentlyUsedBack.prev;
        // recentlyUsedBack.next = null;
    }

    private boolean isStale(CacheNode node) {
        return (System.currentTimeMillis() - node.arrivalTimeMillis) > this.globalExpiryMillis;
    }

    private void clearStaleEntries() {
        if (recentlyUsedFront == recentlyUsedBack && recentlyUsedFront != null) {
            if (isStale(recentlyUsedFront)) {
                recentlyUsedFront = null;
                recentlyUsedBack = null;
            }
        }

        // Remove stale nodes from the ends
        while (isStale(recentlyUsedFront)) {
            removeNode(recentlyUsedFront);
        }
        while (isStale(recentlyUsedBack)) {
            removeNode(recentlyUsedBack);
        }
        // Remove stale nodes from the middle
        if (size() > 2) {
            for (CacheNode curr = recentlyUsedFront.next; curr != recentlyUsedBack.prev; curr = curr.next) {
                if (isStale(curr)) {
                    CacheNode oldPrev = curr.prev;
                    CacheNode oldNext = curr.next;
                    oldPrev.next = oldNext;
                    oldNext.prev = oldPrev;
                    curr = oldNext;
                }
            }
        }
    }

    public int size() {
        return this.cache.size();
    }
}