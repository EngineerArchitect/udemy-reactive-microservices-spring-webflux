package com.reactivespring.sinks;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.stream.IntStream;

public class SinksTest {

    @Test
    /**
     * In this test we are going to publish something and then subscribe to it
     */
    void sink() {
        // Create a replay sink which sent multiple events
        Sinks.Many<Integer> replaySinks = Sinks.many().replay().all();

        // Publish Events
        replaySinks.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySinks.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // Create Subscribe to sink
        Flux<Integer> integerFlux = replaySinks.asFlux();
        integerFlux
                .subscribe(s->{
                    System.out.println("Subscriber 1, Event: " + s);
                });

        Flux<Integer> integerFlux2 = replaySinks.asFlux();
        integerFlux2
                .subscribe(s->{
                    System.out.println("Subscriber 2, Event: " + s);
                });

        // Try and add event after subscription
        replaySinks.tryEmitNext(3); // The Failures is already taken care of so need to resupply FAIL_FAST

        // Using "replay()", everytime you add a new subscriber all events are replayed
        Flux<Integer> integerFlux3 = replaySinks.asFlux();
        integerFlux3
                .subscribe(s->{
                    System.out.println("Subscriber 3, Event: " + s);
                });
    }


    @Test
    void sink_multicast() throws InterruptedException {

        //when

        // It can hold up to 256 elements by default
        Sinks.Many<Integer> multiCast = Sinks.many().multicast().onBackpressureBuffer();

        // Publish Events
        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // Create Subscribe to sink
        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux
                .subscribe(s->{
                    System.out.println("Subscriber 1, Event: " + s);
                });

        // Note that since all events have already been received by (1),
        // this subscriber will only receive Event 3 at the end
        Flux<Integer> integerFlux2 = multiCast.asFlux();
        integerFlux2
                .subscribe(s->{
                    System.out.println("Subscriber 2, Event: " + s);
                });

        // Try and add event after subscription
        multiCast.tryEmitNext(3);
    }

    @Test
    /**
     * Will throw an exception
     * 1... RROR reactor.core.publisher.Operators -- Operator called default onErrorDropped
     * reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
     * Caused by: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
     * 	at reactor.core.publisher.SinkManyUnicast.subscribe(SinkManyUnicast.java:426)
     */
    void sink_unicast() throws InterruptedException {

        //when

        // It can hold up to 256 elements by default
        Sinks.Many<Integer> multiCast = Sinks.many().unicast().onBackpressureBuffer();

        // Publish Events
        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // Create Subscribe to sink
        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux
                .subscribe(s->{
                    System.out.println("Subscriber 1, Event: " + s);
                });

        // Will cause exception
        Flux<Integer> integerFlux2 = multiCast.asFlux();
        integerFlux2
                .subscribe(s->{
                    System.out.println("Subscriber 2, Event: " + s);
                });

        // Try and add event after subscription
        multiCast.tryEmitNext(3);
    }

}