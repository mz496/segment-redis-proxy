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

        jedis = new Jedis(backingRedisAddr, backingRedisPort);
        try {
            jedis.auth(backingRedisPass);
        }
        catch (JedisDataException e) { }
        jedis.connect();
    }

    public String ping() {
        return jedis.ping();
    }

    public String flushDB() {
        return jedis.flushDB();
    }

    public void set(String key, String value) {
        System.out.println("Setting " + key + ", " + value);
        this.cache.set(key,value);
        jedis.set(key,value);
    }

    public String get(String key) {
        String cachedValue = this.cache.get(key);
        if (cachedValue == null) {
            System.out.println("Not found in cache");
            cachedValue = jedis.get(key);
        }
        System.out.println("Got " + cachedValue);
        return cachedValue;
    }

    public int cacheSize() {
        return this.cache.size();
    }
}
