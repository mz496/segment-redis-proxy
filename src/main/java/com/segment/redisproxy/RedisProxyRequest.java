package com.segment.redisproxy;

public class RedisProxyRequest extends Thread {
    private final RedisProxy proxy;

    public RedisProxyRequest(RedisProxy proxy) {
        this.proxy = proxy;
    }
}