# Routing flux design doc

### What does the routing flux do

It conditionally publishes values requested from the source flux into downstream subscribers.
This is what is called "routing". The routing decision can be made on a key that is calculated
for each value in the stream. This key is passed to the function that makes a decision whether to publish the 
value downstream for each registered downstream subscriber. 

### Basic building block

The `RoutingFlux` class can be used as a building block for creating routers for different type of use cases.
For example, in the spike, `FluentRoutingFlux` provides a `route(Predicate<K> interest)` method which can be used to create
a downstream flux. Internally it creates an `EmitterProcessor` that is added as a subscriber to the `RoutingFlux`.

Each use case has special requirements and design decisions on how to handle situations like back pressure.
By default, the `RoutingFlux` stops emitting values when any of the downstream subscribers is back pressured.
