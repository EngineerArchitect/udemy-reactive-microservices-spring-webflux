package com.reactivespring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.reactivespring.repository")
public class MongoConfig {
    // This config will be safely ignored by @WebFluxTest
}
