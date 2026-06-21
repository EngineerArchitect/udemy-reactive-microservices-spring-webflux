package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

// Note: There is no controller
@WebFluxTest
// Inject beans automatically (handler + router)
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    private static final String REVIEWS_URL = "/v1/reviews";

    @MockitoBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        //when
        webTestClient.post().uri(REVIEWS_URL).bodyValue(review).exchange().expectStatus().isCreated().expectBody(Review.class).consumeWith(reviewResponse -> {

            var savedReview = reviewResponse.getResponseBody();
            assert savedReview != null;
            assertNotNull(savedReview.getReviewId());

        });

    }
}
