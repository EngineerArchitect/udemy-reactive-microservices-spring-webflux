package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;

import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {
    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {

        var url = moviesInfoUrl.concat("/{id}");

        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo available for the passed Id: " + movieId,
                                clientResponse.statusCode().value())
                        );
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                    responseMessage, clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                                    "Server Exception in MoviesInfoService " + responseMessage)));
                }))
                .bodyToMono(MovieInfo.class)
                .retry(3)
                .log();
    }

}
