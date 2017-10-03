package com.segment.redisproxy;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.*;

/**
 * 
 *
 */
public class RedisProxy {
    private final Jedis jedis;
    private final RedisProxyCache cache;

    public RedisProxy(
        String backingRedisAddr,
        int backingRedisPort,
        String backingRedisPass,
        int cacheCapacity,
        long globalExpiryMillis) {

        this.cache = new RedisProxyCache(cacheCapacity, globalExpiryMillis);

        this.jedis = new Jedis(backingRedisAddr, backingRedisPort);
        try {
            jedis.auth(backingRedisPass);
        }
        catch (JedisDataException e) { }
        jedis.connect();

        
    }

    public void set(String key, String value) {
        System.out.println("Setting " + key + ", " + value);
        this.cache.set(key,value);
        jedis.set(key,value);
    }

    public String get(String key) {
        System.out.println("====== GET " + key);
        String cachedValue = this.cache.get(key);
        if (cachedValue != null) {
            System.out.println("Value found in cache, moved to front");
            System.out.println("Got " + cachedValue);
            return cachedValue;
        }
        else {
            System.out.println("Value not in cache");
            String value = jedis.get(key);
            if (value != null) {
                System.out.println("Value is in redis but not cache, readding");
                this.cache.set(key,value);
            }
            return value;
        }
    }

    public int cacheSize() {
        return this.cache.size();
    }

    public boolean containsValidEntry(String key) {
        return this.cache.containsValidEntry(key);
    }

    public String ping() {
        return jedis.ping();
    }

    public String flushDB() {
        return jedis.flushDB();
    }
}
