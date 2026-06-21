package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    private static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();
    }


    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        //when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {

                    // then
                    var savedReview = reviewResponse.getResponseBody();
                    assert savedReview != null;
                    assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    @DisplayName("GET /v1/reviews - Should retrieve all 3 reviews in the database")
    void retrieveAllReviews_ReturnsAllReviews() {
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .value(reviews -> assertEquals(3, reviews.size()));
    }

    @Test
    @DisplayName("PUT /v1/reviews/{id} - Should update and return the modified review details")
    void updateReview_ValidReview_ReturnsUpdatedReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var savedReview = reviewReactiveRepository.save(review).block();

        assertNotNull(savedReview, "Saved review should not be null before updating");
        var reviewUpdate = new Review(null, 1L, "Bad Movie", 2.0);

        // when & then
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", savedReview.getReviewId())
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedReview = reviewResponse.getResponseBody();

                    assertNotNull(updatedReview, "Response body should not be null");
                    assertNotNull(savedReview.getReviewId());
                    assertEquals(2.0, updatedReview.getRating());
                    assertEquals("Bad Movie", updatedReview.getComment());
                });
    }

    @Test
    @DisplayName("DELETE /v1/reviews/{id} - Should delete the review and return 204 No Content")
    void deleteReview_ValidId_ReturnsNoContent() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var savedReview = reviewReactiveRepository.save(review).block();

        assertNotNull(savedReview, "Saved review should not be null before deleting");

        // when & then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", savedReview.getReviewId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("GET /v1/reviews?movieInfoId={id} - Should filter and retrieve reviews by movie ID")
    void reviewsByMovieInfoId_ValidMovieInfoId_ReturnsMatchingReviews() {
        // when & then
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(REVIEWS_URL)
                        .queryParam("movieInfoId", "1")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .value(reviewList -> assertEquals(2, reviewList.size()));
    }



}
