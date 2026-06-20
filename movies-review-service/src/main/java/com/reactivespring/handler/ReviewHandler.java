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

    private ReviewReactiveRepository reviewReactiveRepository;

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
}
