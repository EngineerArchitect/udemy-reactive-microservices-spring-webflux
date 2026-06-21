package com.reactivespring.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Description;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

// What are you testing against
@WebFluxTest(controllers = FluxMonoController.class)
// Auto-inject a rest client for testing
@AutoConfigureWebTestClient
class FluxMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Description("Test Flux Endpoint")
    @DisplayName("Should return flux of 3 integers - successful response")
    void should_return_flux_of_3_integers_when_calling_flux_endpoint() {

        webTestClient.get()
                .uri("/flux")
                .exchange() // Calls endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(3);

    }

    @Test
    @Description("Test Flux Endpoint Alternative approach 2")
    @DisplayName("Test 2")
    void flux_approach_2() {

        var flux = webTestClient.get()
                .uri("/flux")
                .exchange() // Calls endpoint
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(1,2,3)
                .verifyComplete();
    }

    @Test
    @Description("Test Flux Endpoint Alternative approach 3")
    @DisplayName("Test 3")
    void flux_approach_3() {

        webTestClient.get()
                .uri("/flux")
                .exchange() // Calls endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assert (Objects.requireNonNull(responseBody).size() == 3);
                });
    }

    @Test
    @Description("Test Mono Approact 3")
    @DisplayName("Test 4 - Mono")
    void mono_approach() {

        webTestClient.get()
                .uri("/mono")
                .exchange() // Calls endpoint
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("hello-world", responseBody);
                });
    }

    /**
     * Copied from approach 2
     */
    @Test
    void should_stream() {

        var flux = webTestClient
                .get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }
}