package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

// Note: There is no controller
@WebFluxTest
// Inject beans automatically (handler + router)
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
@Nested
@DisplayName("GET /v1/reviews endpoint tests")
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

    @Test
    @DisplayName("Should perform validation on add review and fail on invalid review")
    void addReview_Validations() {
        //given
        var review = new Review(null, null, "Awesome Movie", -9.0);

        //when
        webTestClient
                .post()
                .uri("/v1/reviews")
                .bodyValue(review)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null, rating.negative : please pass a non-negative value");
    }

    @Test
    @DisplayName("Should return all reviews when reviews exist")
    void givenReviewsExist_whenGetAllReviews_thenReturnAllReviews() {
        //given
        var reviewList = List.of(
                new Review(null, 1L, "Mediocre film", 6.0),
                new Review(null, 2L, "Badly Directed", 4.0),
                new Review(null, 3L, "Boring as Hell", 3.0),
                new Review(null, 3L, "Excellent movie", 9.0));

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));

        //when
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviews -> {
                    assertEquals(4, reviews.size());
                });
    }

    @Test
    @DisplayName("Should update and return review")
    void givenReviewExist_whenUpdateReview_thenUpdateAndreturnReview() {
        //given
        var reviewUpdate = new Review(null, 1L, "What was the director thinking", 3.0);

        // When
        when(reviewReactiveRepository.findById((String) any())).thenReturn(Mono.just(new Review("abc", 1L, "Brilliant Movie", 9.0)));
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "What was the director thinking", 3.0)));


        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {

                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    assertEquals("abc", updatedReview.getReviewId());
                    assertEquals(3.0, updatedReview.getRating());
                    assertEquals(1L, updatedReview.getMovieInfoId());
                    assertEquals("What was the director thinking", updatedReview.getComment());
                });

    }

    @Test
    @DisplayName("Should delete movie review")
    void givenReviewExist_whenDeleteReview_thenDeleteReviewAndReturnOk() {
        //given
        var reviewId= "abc";
        when(reviewReactiveRepository.findById((String) any())).thenReturn(Mono.just(new Review("abc", 1L, "Brilliant Film", 9.0)));
        when(reviewReactiveRepository.deleteById((String) any())).thenReturn(Mono.empty());

        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent();
    }


    @Test
    @DisplayName("Should return all reviews with given MovieInfoId")
    void givenReviewsExist_whenGetReviewsWithMovieInfoId_thenReturnReviewWithMovieInfoId() {
        //given
        var reviewsWithMovieIdList = List.of(
                new Review(null, 1L, "Mediocre film", 6.0),
                new Review(null, 1L, "Badly Directed", 4.0));

        when(reviewReactiveRepository.findReviewsByMovieInfoId(1L)).thenReturn(Flux.fromIterable(reviewsWithMovieIdList));

        //when
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REVIEWS_URL)
                        .queryParam("movieInfoId", "1")
                        .build())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviews -> {
                    assertEquals(2, reviews.size());
                });
    }

}
