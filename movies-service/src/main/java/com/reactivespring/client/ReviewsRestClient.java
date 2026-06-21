package com.reactivespring.client;

import com.reactivespring.domain.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class ReviewsRestClient {
    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {

        var url = UriComponentsBuilder.fromUriString(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .build()
                .toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class);
    }

}
