package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorReview;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TutorReviewRepository extends ReactiveCrudRepository<TutorReview, Long> {

    Flux<TutorReview> findByTutorId(Long tutorId);

    @Query("""
        SELECT AVG(rating)
        FROM reviews
        WHERE reviewee_id = :tutorId
    """)
    Mono<BigDecimal> avgRating(Long tutorId);

    Mono<Long> countByTutorId(Long tutorId);
}
