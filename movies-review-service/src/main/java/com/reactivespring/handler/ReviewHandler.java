package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                // When you are going to do a reactive operation and return a value use a flatMap
                // review -> reviewReactiveRepository.save(review)
                .flatMap(reviewReactiveRepository::save)
                // Required to convert from Mono<Object> to Mono<ServerResponse>
                // savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var reviewsFlux = reviewReactiveRepository.findAll();

        // Use the body(P publisher, Class<T> elementClass), using reviewsFlux as publisher
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId);
        //.switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not Found for the given Review Id")));

        return existingReview
                .flatMap(review -> serverRequest.bodyToMono(Review.class)
                        // Update the existing review with the new one from the http request
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(ServerResponse.ok()::bodyValue));
    }
}
