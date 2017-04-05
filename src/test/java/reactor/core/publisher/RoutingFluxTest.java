package reactor.core.publisher;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.subscriber.AssertSubscriber;
import reactor.util.concurrent.QueueSupplier;

import java.util.concurrent.CancellationException;

public class RoutingFluxTest {
    @Test
    public void normal() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create();
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create();

        ConnectableFlux<Integer> p = RoutingFlux.create(Flux.range(1, 5));

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void normalBackpressured() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create(0);
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create(0);

        ConnectableFlux<Integer> p = RoutingFlux.create(Flux.range(1, 5));

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts1.request(3);
        ts2.request(2);

        ts1.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts2.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts1.request(2);
        ts2.request(3);

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void normalAsyncFused() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create();
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create();

        UnicastProcessor<Integer> up = UnicastProcessor.create(QueueSupplier.<Integer>get(8).get());
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        up.onComplete();

        ConnectableFlux<Integer> p = RoutingFlux.create(up);

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void normalBackpressuredAsyncFused() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create(0);
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create(0);

        UnicastProcessor<Integer> up = UnicastProcessor.create(QueueSupplier.<Integer>get(8).get());
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        up.onComplete();

        ConnectableFlux<Integer> p = RoutingFlux.create(up);

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts1.request(3);
        ts2.request(2);

        ts1.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts2.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts1.request(2);
        ts2.request(3);

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void normalHidden() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create();
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create();

        ConnectableFlux<Integer> p = RoutingFlux.create(Flux.range(1, 5), 5);

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void normalHiddenBackpressured() {
        AssertSubscriber<Integer> ts1 = AssertSubscriber.create(0);
        AssertSubscriber<Integer> ts2 = AssertSubscriber.create(0);

        ConnectableFlux<Integer> p = RoutingFlux.create(Flux.range(1, 5), 5);

        p.subscribe(ts1);
        p.subscribe(ts2);

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        p.connect();

        ts1
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts2
                .assertNoValues()
                .assertNoError()
                .assertNotComplete();

        ts1.request(3);
        ts2.request(2);

        ts1.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts2.assertValues(1, 2)
                .assertNoError()
                .assertNotComplete();

        ts1.request(2);
        ts2.request(3);

        ts1.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();

        ts2.assertValues(1, 2, 3, 4, 5)
                .assertNoError()
                .assertComplete();
    }

    @Test
    public void disconnect() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        EmitterProcessor<Integer> e = EmitterProcessor.create();
        e.connect();

        ConnectableFlux<Integer> p = RoutingFlux.create(e);

        p.subscribe(ts);

        Disposable r = p.connect();

        e.onNext(1);
        e.onNext(2);

        r.dispose();

        ts.assertValues(1, 2)
                .assertError(CancellationException.class)
                .assertNotComplete();

        Assert.assertFalse("sp has subscribers?", e.downstreamCount() != 0);
    }

    @Test
    public void disconnectBackpressured() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);

        EmitterProcessor<Integer> e = EmitterProcessor.create();
        e.connect();

        ConnectableFlux<Integer> p = RoutingFlux.create(e);

        p.subscribe(ts);

        Disposable r = p.connect();

        r.dispose();

        ts.assertNoValues()
                .assertError(CancellationException.class)
                .assertNotComplete();

        Assert.assertFalse("sp has subscribers?", e.downstreamCount() != 0);
    }

    @Test
    public void error() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        EmitterProcessor<Integer> e = EmitterProcessor.create();
        e.connect();

        ConnectableFlux<Integer> p = RoutingFlux.create(e);

        p.subscribe(ts);

        p.connect();

        e.onNext(1);
        e.onNext(2);
        e.onError(new RuntimeException("forced failure"));

        ts.assertValues(1, 2)
                .assertError(RuntimeException.class)
                .assertErrorWith( x -> Assert.assertTrue(x.getMessage().contains("forced failure")))
                .assertNotComplete();
    }

    @Test
    public void fusedMapInvalid() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();

        ConnectableFlux<Integer> p = RoutingFlux.create(Flux.range(1, 5).map(v -> (Integer)null));

        p.subscribe(ts);

        p.connect();

        ts.assertNoValues()
                .assertError(NullPointerException.class)
                .assertNotComplete();
    }


    @Test
    public void retry() {
        DirectProcessor<Integer> dp = DirectProcessor.create();
        StepVerifier.create(
                dp.publish()
                        .autoConnect().<Integer>handle((s1, sink) -> {
                    if (s1 == 1) {
                        sink.error(new RuntimeException());
                    }
                    else {
                        sink.next(s1);
                    }
                }).retry())
                .then(() -> {
                    dp.onNext(1);
                    dp.onNext(2);
                    dp.onNext(3);
                })
                .expectNext(2, 3)
                .thenCancel()
                .verify();

        // Need to explicitly complete processor due to use of publish()
        dp.onComplete();
    }

    @Test
    public void retryWithPublishOn() {
        DirectProcessor<Integer> dp = DirectProcessor.create();
        StepVerifier.create(
                dp.publishOn(Schedulers.parallel()).publish()
                        .autoConnect().<Integer>handle((s1, sink) -> {
                    if (s1 == 1) {
                        sink.error(new RuntimeException());
                    }
                    else {
                        sink.next(s1);
                    }
                }).retry())
                .then(() -> {
                    dp.onNext(1);
                    dp.onNext(2);
                    dp.onNext(3);
                })
                .expectNext(2, 3)
                .thenCancel()
                .verify();

        // Need to explicitly complete processor due to use of publish()
        dp.onComplete();
    }


}
