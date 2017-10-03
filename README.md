# Redis Proxy

## High-level architecture overview

This is a proxy that caches GET requests for a single backing Redis instance. Any GET requests will first pass through the cache to determine whether it needs to call on the backing Redis instance. All SET requests bypass the proxy.

The cache is limited in size and is defined by two parameters, the capacity and the global expiry. The capacity defines how many keys can be stored in the cache, and the global expiry defines how long is allowed to pass before a key is expired, which is thereafter treated as if it weren't in the cache. When the cache is at capacity, items are evicted according to a least-recently-used policy.

## What the code does

The cache has a doubly linked list field that maintains the order of usage of key-value pairs. The doubly linked list is treated like a queue that moves nodes to the front as they are used and cuts nodes off the back as they are evicted.

The cache is implemented as a HashMap from Strings to CacheNodes, which are the nodes in the doubly linked list described above. The cache thus stores its values purely as key-value pairs.

## Instructions for running the proxy and tests

Run `sudo docker daemon` in one terminal window.

In another terminal window, run `make test` in the main project directory.
Prerequisites: A Redis Docker image installed, and a recent version of docker installed.

## Time breakdown

Understanding requirements: ~30 minutes

Setting up Maven, Redis, and Docker: ~2 hours

Total time on all features of cache: ~2 hours

Total time on unit tests: ~2 hours

Documentation: ~1 hour

## Unimplemented requirements

I couldn't find a good way to launch the Docker daemon in the background, which I think needs to be running in order to launch the Redis instance. Mostly due to time issues.

My code assumes that a Redis Docker image is already installed for convenience's sake.

My code also only stores strings as values, instead of storing general data structures, mostly because of time issues.

Finally, I'm not entirely sure if my concurrency is implemented correctly, since I seem to be implementing most of the concurrency inside a unit test instead. I also was unable to figure out the details because of time issues.