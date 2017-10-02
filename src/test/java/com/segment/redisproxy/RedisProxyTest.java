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
        System.out.println("Proxy can connect to server: " + proxy.ping());
    }

    /**
     * 
     */
    public void testSetGet() {
        System.out.println("Running testSetGet");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 0, 0);

        System.out.println("Proxy can connect to server: " + proxy.ping());
        proxy.set("asdf","fdsa");
        proxy.get("asdf");

    }
}
