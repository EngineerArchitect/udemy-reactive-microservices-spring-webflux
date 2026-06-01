package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@DataMongoTest
@Testcontainers
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationIntgTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        // given

        // when
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        // then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        // given

        // when
        var moviesInfoMono = movieInfoRepository.findById("abc")
                .log();

        // then
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    Assertions.assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        // given
        var movieInfo = new MovieInfo(null, "The Batman", 2005,
                List.of("Robert Pattinson", "Zoë Kravitz"), LocalDate.parse("2022-03-01"));

        // when
        var moviesInfoMono = movieInfoRepository.save(movieInfo)
                .log();

        // then
        StepVerifier.create(moviesInfoMono)
                .assertNext(mi -> {
                    Assertions.assertNotNull(mi.getMovieInfoId());
                    Assertions.assertEquals("The Batman", mi.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        var savedMovieInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(savedMovieInfo)
                .assertNext(movieInfo1 -> {
                    Assertions.assertNotNull(movieInfo1.getMovieInfoId());
                    Assertions.assertEquals(2021, movieInfo1.getYear());
                });

    }

    @Test
    void deleteMovieInfo() {

        movieInfoRepository.deleteById("abc").block();

        var movieInfos = movieInfoRepository.findAll();

        StepVerifier.create(movieInfos)
                .expectNextCount(2)
                .verifyComplete();

    }

    @Test
    void findMovieInfoByYear() {

        var movieInfosFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(movieInfosFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {

        var movieInfosMono = movieInfoRepository.findByName("Batman Begins").log();

        StepVerifier.create(movieInfosMono)
                .expectNextCount(1)
                .verifyComplete();
    }
}