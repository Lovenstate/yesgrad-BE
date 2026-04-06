package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Rating;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingRepository extends ReactiveCrudRepository<Rating, Long> {

    @Query("SELECT AVG(rating) FROM ratings WHERE tutor_id = :tutorId")
    Mono<Double> getAverageRatingByTutor(Long tutorId);

    @Query("SELECT COUNT(*) FROM ratings WHERE tutor_id = :tutorId")
    Mono<Long> countRatingsByTutor(Long tutorId);

    @Query("SELECT * FROM ratings WHERE tutor_id = :tutorId ORDER BY created_at DESC LIMIT :limit")
    Flux<Rating> findRecentRatingsByTutor(Long tutorId, int limit);
}
