package reactor.core.publisher;

import org.reactivestreams.Subscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FluentRoutingFlux<T, K> extends RoutingFlux<T, K> {
    private static class RoutingRegistry<T, K> {
        private final Map<Subscriber<? super T>, Predicate<K>> interests = new HashMap<>();
        final BiPredicate<Subscriber<? super T>, K> filter = (subscriber, k) -> interests.getOrDefault(subscriber, testVal -> false).test(k);
        final Consumer<Subscriber<? super T>> onSubscriberRemoved = subscriber -> interests.remove(subscriber);

        void registerSubscriber(Subscriber<? super T> subscriber, Predicate<K> interestFunction) {
            interests.put(subscriber, interestFunction);
        }
    }

    public static <T, K> FluentRoutingFlux<T, K> create(Flux<? extends T> source, int prefetch, Supplier<? extends
            Queue<T>> queueSupplier, Function<? super T, K> routingKeyFunction) {
        return new FluentRoutingFlux<T, K>(source, prefetch, queueSupplier, routingKeyFunction,
                new RoutingRegistry<>());
    }

    private final RoutingRegistry<T, K> routingRegistry;

    FluentRoutingFlux(Flux<? extends T> source, int prefetch, Supplier<? extends Queue<T>> queueSupplier, Function<?
            super T, K> routingKeyFunction, RoutingRegistry<T, K> routingRegistry) {
        super(source, prefetch, queueSupplier, routingKeyFunction, routingRegistry.filter,
                subscriber -> {}, routingRegistry.onSubscriberRemoved);
        this.routingRegistry = routingRegistry;
    }

    public Flux<T> route(Predicate<K> interest) {
        FluxProcessor<T, T> fluxProcessor = EmitterProcessor.create();
        routingRegistry.registerSubscriber(fluxProcessor, interest);
        subscribe(fluxProcessor);
        return fluxProcessor;
    }
}
