# This doc is outdated, [more recent doc in reactor-addons](https://github.com/lhotari/reactor-addons/blob/lh-routingflux/docs/src/docs/asciidoc/routing.adoc)

# Routing flux design doc

### What does the routing flux do

It conditionally emits values received from the source flux into downstream subscribers.
This is what is called "routing". The routing decision can be made on a key that is calculated once
for each source value in the stream. This key is passed to a function that makes the decision of emitting the source
value to downstream subscribers. 

### Basic building block

The [`RoutingFlux`](src/main/java/reactor/core/publisher/RoutingFlux.java) class can be used as a building block for 
creating routers for different type of use cases. Most of the source code for `RoutingFlux` originates from 
[`FluxPublish`](src/main/java/reactor/core/publisher/FluxPublish.java).

In the spike, [`PredicateRoutingFlux`](src/main/java/reactor/core/publisher/PredicateRoutingFlux.java)
provides a `route(Predicate<K> interest)` method which can be used to create
a downstream flux. Internally it creates an `EmitterProcessor` that is added as a subscriber to the `RoutingFlux`.
[`KeyedRoutingFlux`](src/main/java/reactor/core/publisher/KeyedRoutingFlux.java) is an example of optimized 
keyed routing. The benefit of it over `PredicateRoutingFlux` is that the routing decision can be made without 
executing a predicate function for every subscriber for every value that is emitted to the downstream subscribers. 

### Design problems

#### Each filter has to be evaluated once

Solution: pass all active subscribers as a stream to selection function. Should return a stream of filtered subscribers 
back.
This allows implementing optimized keyed routing when it's required, without changing the underlying basic building 
block. See [`KeyedRoutingFlux`](src/main/java/reactor/core/publisher/KeyedRoutingFlux.java) for an example.

#### Backpressure in a single downstream subscriber stops the stream

Currently when any downstream subscriber gets backpressured, it will stop the `RoutingFlux` stream 
and no values get emitted to any downstream subscriber. This is a safe default, but might be a problem 
for some use cases and could be confusing from the developer's perspective if there's not solutions for easily 
configuring backpressure dropping in these cases. Currently every downstream would have to contain a separate 
operator for dropping values on backpressure to be able to configure this. The alternative would be to support
configuring backpressure on the emitting side. It's a fairly easy change to `RoutingFlux` to handle this, but 
is it adding unnecessary complexity?

### API examples

[`PredicateRoutingFlux`](src/main/java/reactor/core/publisher/PredicateRoutingFlux.java) example
```java
PredicateRoutingFlux<Integer, Integer> routingFlux = PredicateRoutingFlux.create(Flux.range(1, 5),
        QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), 
        Function.identity());

Flux<Integer> evenFlux = routingFlux.route(x -> x % 2 == 0);
Flux<Integer> oddFlux = routingFlux.route(x -> x % 2 != 0);

routingFlux.connect();

Mono<List<Integer>> evenListMono = evenFlux.collectList().subscribe();
Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

assertEquals(Arrays.asList(2, 4), evenListMono.block());
assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
```

[`KeyedRoutingFlux`](src/main/java/reactor/core/publisher/KeyedRoutingFlux.java) example
```java
KeyedRoutingFlux<Integer, Integer> routingFlux = KeyedRoutingFlux.create(Flux.range(1, 5),
        QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), 
        value -> value % 2);

Flux<Integer> evenFlux = routingFlux.route(0);
Flux<Integer> oddFlux = routingFlux.route(1);

routingFlux.connect();

Mono<List<Integer>> evenListMono = evenFlux.collectList().subscribe();
Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

assertEquals(Arrays.asList(2, 4), evenListMono.block());
assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
```
