# Redis Proxy

## High-level architecture overview

This is a proxy that caches GET requests for a single backing Redis instance. Any GET requests will first pass through the cache to determine whether it needs to call on the backing Redis instance. All SET requests bypass the proxy.

The cache is limited in size and is defined by two parameters, the capacity and the global expiry. The capacity defines how many keys can be stored in the cache, and the global expiry defines how long is allowed to pass before a key is expired, which is thereafter treated as if it weren't in the cache. When the cache is at capacity, items are evicted according to a least-recently-used policy.

Requests to the backing Redis instance can be sent sequentially with a concurrent runner class or with an instance of the proxy. Requests can be sent concurrently only with a concurrent runner class, although they will be handled sequentially.

## What the code does

The cache has a doubly linked list field that maintains the order of usage of key-value pairs. The doubly linked list is treated like a queue that moves nodes to the front as they are used and cuts nodes off the back as they are evicted.

The cache is implemented as a HashMap from Strings to CacheNodes, which are the nodes in the doubly linked list described above. The cache thus stores its values purely as key-value pairs.

Concurrent request acceptance is implemented with a thread pool of size one (a single thread executor in the Java Concurrent library).

## Instructions for running the proxy and tests

### Prerequisites

* Docker
* Docker-Compose
* Sufficient permissions to run the commands `docker` and `docker-compose` on your user
* Java 8
* Make

### Running code

All commands should be run from the project's root directory.

In one terminal window, run `docker daemon` in one terminal window.

In a second terminal window, run `docker-compose up` in another terminal window.

In a third terminal window, run `make test`.

You should see the output from JUnit that reports the tests passed.

## Time breakdown

Understanding requirements: ~30 minutes

Setting up Maven, Redis, and Docker: ~2 hours

Total time on all features of cache: ~2 hours

Total time on unit tests: ~2 hours

Documentation: ~1 hour

Revision: ~1 hour

## Unimplemented requirements

My code only stores strings as values, instead of storing general data structures, mostly because of time issues.
