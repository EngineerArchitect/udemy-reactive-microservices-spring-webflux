package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler) {
        // Configure a router with a handler as lambda
        return route()
                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("HelloWorld")))
                .nest(path("v1/reviews"), builder -> {
                    builder.POST("", reviewHandler::addReview) // request -> reviewHandler.addReview(request)
                            .GET("", reviewHandler::getReviews)
                            .PUT("/{id}", reviewHandler::updateReview);
                })
                .build();
    }
}
