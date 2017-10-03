package com.segment.redisproxy;

import org.junit.Test;
import static org.junit.Assert.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.*;
import java.lang.*;

/**
 * Unit tests for the Redis proxy.
 */
public class RedisProxyTest {
    /**
     * Tests whether the proxy can connect to Redis
     */
    @Test
    public void testProxyCanConnect() {
        System.out.println("Running testProxyCanConnect");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 0, 0);
        assertEquals(proxy.ping(), "PONG");
        proxy.flushDB();
    }

    /**
     * Test simple get/set
     */
    @Test
    public void testHappyCase() {
        System.out.println("Running testHappyCase");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, 10000);
        assertEquals(proxy.ping(), "PONG");

        assertNull(proxy.get("a"));
        assertNull(proxy.get("asdf"));
        proxy.set("asdf","fdsa");
        assertEquals(proxy.get("asdf"), "fdsa");
        proxy.flushDB();
    }

    public void testHappyCaseTimeout() {

    }

    public void testUpdateKey() {

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCacheSize() {
        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", -1, 10000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidExpiry() {
        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, -1);
    }
    
    // Test cache at low capacity

    // Test multiple sets of the same key

    // Test cache of invalid size

    // Test invalid global expiry

    // Test happy case

    // Test expiring entries

    // Test full of expiring entries

    // Test concurrent requests

    public void testLowCapacityCache() {
        System.out.println("Running testLowCapacityCache");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 1, 10000);
        proxy.set("a","1");
        assertEquals(proxy.get("a"), "1");
        assertEquals(proxy.cacheSize(), 1);
        proxy.set("b","2");
        assertEquals(proxy.get("b"), "2");
        assertEquals(proxy.cacheSize(), 1);
        assertEquals(proxy.get("a"), "1");
        assertEquals(proxy.cacheSize(), 1);
        proxy.flushDB();
    }


}
