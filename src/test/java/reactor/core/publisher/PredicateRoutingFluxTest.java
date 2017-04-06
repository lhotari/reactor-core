package reactor.core.publisher;

import org.junit.Test;
import reactor.util.concurrent.QueueSupplier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PredicateRoutingFluxTest {

    @Test
    public void supportFluentRoutingSyntax() {
        PredicateRoutingFlux<Integer, Integer> routingFlux = PredicateRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        Flux<Integer> evenFlux = routingFlux.route(x -> x % 2 == 0);
        Flux<Integer> oddFlux = routingFlux.route(x -> x % 2 != 0);

        routingFlux.connect();

        Mono<List<Integer>> evenListMono = evenFlux.collectList().subscribe();
        Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

        assertEquals(Arrays.asList(2, 4), evenListMono.block());
        assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
    }

    @Test
    public void fluentRoutingSubscribePartiallyLastUnconsumed() {
        PredicateRoutingFlux<Integer, Integer> routingFlux = PredicateRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        Flux<Integer> evenFlux = routingFlux.route(x -> x % 2 == 0);
        routingFlux.route(x -> x % 2 != 0).log().onBackpressureDrop().subscribe(); // this is unused to test partially subscribed downstream fan-out

        routingFlux.connect();

        MonoProcessor<List<Integer>> evenListMono = evenFlux.collectList().subscribe();

        assertEquals(Arrays.asList(2, 4), evenListMono.block());
    }

    @Test
    public void fluentRoutingSubscribePartiallyLastConsumed() {
        PredicateRoutingFlux<Integer, Integer> routingFlux = PredicateRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        routingFlux.route(x -> x % 2 == 0).log().onBackpressureDrop().subscribe();
        Flux<Integer> oddFlux = routingFlux.route(x -> x % 2 != 0).log();

        routingFlux.connect();

        Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

        assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
    }

}
