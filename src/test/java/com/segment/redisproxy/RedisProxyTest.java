package com.segment.redisproxy;

import org.junit.*;
import static org.junit.Assert.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.*;
import java.lang.*;
import java.util.concurrent.*;

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
    public void testSimpleGetSet() {
        System.out.println("Running testSimpleGetSet");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, 10000);

        assertFalse(proxy.containsValidEntry("asdf"));
        proxy.set("asdf","fdsa");
        assertTrue(proxy.containsValidEntry("asdf"));
        assertEquals(proxy.get("asdf"), "fdsa");
        proxy.flushDB();
    }

    @Test
    public void testSimpleTimeout() throws InterruptedException {
        System.out.println("Running testSimpleTimeout");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, 100);

        assertFalse(proxy.containsValidEntry("a"));
        proxy.set("a","1");
        assertEquals(proxy.cacheSize(), 1);
        Thread.sleep(200);
        assertFalse(proxy.containsValidEntry("a"));
        proxy.flushDB();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCacheSize() {
        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", -1, 10000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidExpiry() {
        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, -1);
    }

    @Test
    public void testUpdateKey() {
        System.out.println("Running testUpdateKey");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 10, 10000);

        assertFalse(proxy.containsValidEntry("a"));
        proxy.set("a","1");
        proxy.set("a","2");
        assertEquals(proxy.cacheSize(), 1);
        assertEquals(proxy.get("a"), "2");
        proxy.flushDB();
    }

    @Test
    public void testOverflowCache() {
        System.out.println("Running testOverflowCache");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 1, 10000);
        
        assertFalse(proxy.containsValidEntry("a"));
        assertFalse(proxy.containsValidEntry("b"));
        proxy.set("a","1");
        assertTrue(proxy.containsValidEntry("a"));
        assertEquals(proxy.cacheSize(), 1);
        proxy.set("b","2");
        assertTrue(proxy.containsValidEntry("b"));
        assertFalse(proxy.containsValidEntry("a"));
        assertEquals(proxy.cacheSize(), 1);

        // Cache is full but we can still get items stored earlier
        assertEquals(proxy.get("a"), "1");
        assertEquals(proxy.get("b"), "2");
        proxy.flushDB();
    }

    @Test
    public void testEvictionOrder() {
        System.out.println("Running testEvictionOrder");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 4, 10000);

        proxy.set("a","1");
        proxy.set("b","2");
        proxy.set("c","3");
        proxy.set("d","4");
        proxy.set("e","5");
        // Cache should contain FRONT e d c b BACK
        assertTrue(proxy.containsValidEntry("e"));
        assertTrue(proxy.containsValidEntry("d"));
        assertTrue(proxy.containsValidEntry("c"));
        assertTrue(proxy.containsValidEntry("b"));
        assertFalse(proxy.containsValidEntry("a"));
        assertEquals(proxy.cacheSize(), 4);

        proxy.get("c");
        proxy.set("c","10");
        proxy.get("d");
        proxy.get("b");
        // Cache should contain FRONT b d c e BACK
        proxy.get("a");
        proxy.set("f","12");
        // Cache should contain FRONT f a b d BACK
        assertTrue(proxy.containsValidEntry("f"));
        assertTrue(proxy.containsValidEntry("a"));
        assertTrue(proxy.containsValidEntry("b"));
        assertTrue(proxy.containsValidEntry("d"));
        assertFalse(proxy.containsValidEntry("e"));
        assertFalse(proxy.containsValidEntry("c"));
        assertEquals(proxy.cacheSize(), 4);

        proxy.flushDB();
    }

    @Test
    public void testDifferentTimeouts() throws InterruptedException {
        System.out.println("Running testDifferentTimeouts");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 4, 100);

        proxy.set("a","1");
        proxy.set("b","2");
        proxy.set("c","3");
        proxy.set("d","4");
        assertEquals(proxy.cacheSize(), 4);
        // Cache should contain a,b,c,d (all 0 ms old)

        Thread.sleep(70);
        proxy.set("a","5");
        proxy.set("b","6");
        assertEquals(proxy.cacheSize(), 4);
        // Cache should contain a,b (0 ms old), c,d (70 ms old)

        Thread.sleep(70);
        proxy.set("e","5");
        proxy.set("f","6");
        assertEquals(proxy.cacheSize(), 4);
        // Cache should contain a,b (70 ms old), e,f (0 ms old) [c,d expired (140 ms old)]

        assertTrue(proxy.containsValidEntry("a"));
        assertTrue(proxy.containsValidEntry("b"));
        assertFalse(proxy.containsValidEntry("c"));
        assertFalse(proxy.containsValidEntry("d"));
        assertTrue(proxy.containsValidEntry("e"));
        assertTrue(proxy.containsValidEntry("f"));
        assertEquals(proxy.cacheSize(), 4);
        proxy.flushDB();
    }

    @Test
    public void testConcurrentClients() throws InterruptedException {
        System.out.println("Running testConcurrentClients");

        RedisProxy proxy = new RedisProxy("localhost", 6379, "foobared", 4, 100);

        // ExecutorService service = Executors.newSingleThreadExecutor();
        ExecutorService service = Executors.newFixedThreadPool(1);
        RedisProxyRequest request1 = new RedisProxyRequest(proxy) {
            @Override
            public void run() {
                proxy.set("a","1");
                proxy.set("b","2");
                proxy.set("c","3");
            }
        };
        RedisProxyRequest request2 = new RedisProxyRequest(proxy) {
            @Override
            public void run() {
                proxy.set("a","4");
                proxy.set("b","5");
                proxy.set("c","6");
            }
        };
        RedisProxyRequest request3 = new RedisProxyRequest(proxy) {
            @Override
            public void run() {
                proxy.set("a","7");
                proxy.set("b","8");
                proxy.set("c","9");
            }
        };
        service.execute(request1);
        service.execute(request2);
        service.execute(request3);
        Thread.sleep(1000);
        assertEquals(proxy.get("a"),"7");
        assertEquals(proxy.get("b"),"8");
        assertEquals(proxy.get("c"),"9");
        proxy.flushDB();

    }
    

    // Test cache at low capacity

    // Test multiple sets of the same key

    // Test cache of invalid size

    // Test invalid global expiry

    // Test happy case

    // Test expiring entries

    // Test full of expiring entries

    // Test concurrent requests




}
