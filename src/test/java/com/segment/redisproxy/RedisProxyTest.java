package com.segment.redisproxy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.*;

/**
 * Unit test for the Redis proxy.
 */
public class RedisProxyTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RedisProxyTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(RedisProxyTest.class);
    }

    /**
     * Tests whether the proxy can connect to Redis
     */
    public void testProxyCanConnect() {
        System.out.println("Running testProxyCanConnect");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 0, 0);
        assertEquals(proxy.ping(), "PONG");
    }

    /**
     * 
     */
    public void testSetGet() {
        System.out.println("Running testSetGet");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, 10000);
        assertEquals(proxy.ping(), "PONG");

        assertNull(proxy.get("asdf"));
        proxy.set("asdf","fdsa");
        assertEquals(proxy.get("asdf"), "fdsa");
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
    }


}
