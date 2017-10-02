package com.segment.redisproxy;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.*;

/**
 * 
 *
 */
public class RedisProxy {
    private final Jedis jedis;
    private final int cacheCapacity;
    private final int globalExpiry;
    private final RedisProxyCache cache;

    public RedisProxy(
        String backingRedisAddr,
        int backingRedisPort,
        String backingRedisPass,
        int cacheCapacity,
        int globalExpiry) {

        this.cacheCapacity = cacheCapacity;
        this.globalExpiry = globalExpiry;
        this.cache = new RedisProxyCache(cacheCapacity);

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

    public void set(String key, String value) {
        System.out.println("Setting " + key + ", " + value);
        jedis.set(key,value);
    }

    public void get(String key) {
        String value = jedis.get(key);
        System.out.println("Got " + value);
    }
}
