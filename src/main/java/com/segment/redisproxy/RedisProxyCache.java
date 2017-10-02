package com.segment.redisproxy;

import java.util.*;

class CacheNode {
    private CacheNode prev;
    private CacheNode next;
    private String value;

    public CacheNode(String value) {

    }
}

public class RedisProxyCache {
    private HashMap<String, CacheNode> cache;
    private int capacity;

    public RedisProxyCache(int capacity) {
        this.cache = new HashMap<>();
        this.capacity = capacity;
    }
}