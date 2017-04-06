package reactor.core.publisher;

import org.junit.Test;
import reactor.util.concurrent.QueueSupplier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class FluentRoutingFluxTest {

    @Test
    public void supportFluentRoutingSyntax() {
        FluentRoutingFlux<Integer, Integer> routingFlux = FluentRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        Consumer<Integer> onDrop = x -> System.out.println("dropped " + x);

        Flux<Integer> evenFlux = routingFlux.route(x -> x % 2 == 0, onDrop);
        Flux<Integer> oddFlux = routingFlux.route(x -> x % 2 != 0, onDrop);


        Mono<List<Integer>> evenListMono = evenFlux.collectList().subscribe();
        Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

        routingFlux.connect();

        assertEquals(Arrays.asList(2, 4), evenListMono.block());
        assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
    }

    @Test
    public void fluentRoutingSubscribePartiallyLastUnconsumed() {
        FluentRoutingFlux<Integer, Integer> routingFlux = FluentRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        Consumer<Integer> onDrop = x -> System.out.println("dropped " + x);

        Flux<Integer> evenFlux = routingFlux.route(x -> x % 2 == 0, onDrop);
        routingFlux.route(x -> x % 2 != 0, onDrop);
        // this is unused to test
        // partially subscribed
        // downstream fan-out


        MonoProcessor<List<Integer>> evenListMono = evenFlux.collectList().subscribe();

        routingFlux.connect();

        assertEquals(Arrays.asList(2, 4), evenListMono.block());
    }

    @Test
    public void fluentRoutingSubscribePartiallyLastConsumed() {
        FluentRoutingFlux<Integer, Integer> routingFlux = FluentRoutingFlux.create(Flux.range(1, 5),
                QueueSupplier.SMALL_BUFFER_SIZE, QueueSupplier.get(QueueSupplier.SMALL_BUFFER_SIZE), value -> value);

        Consumer<Integer> onDrop = x -> System.out.println("dropped " + x);
        routingFlux.route(x -> x % 2 == 0, onDrop).onBackpressureDrop().subscribe();
        Flux<Integer> oddFlux = routingFlux.route(x -> x % 2 != 0, onDrop).log();


        Mono<List<Integer>> oddListMono = oddFlux.collectList().subscribe();

        routingFlux.connect();

        assertEquals(Arrays.asList(1, 3, 5), oddListMono.block());
    }

}
