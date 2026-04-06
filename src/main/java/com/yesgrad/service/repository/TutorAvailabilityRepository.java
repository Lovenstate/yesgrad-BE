package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorAvailability;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorAvailabilityRepository extends ReactiveCrudRepository<TutorAvailability, Long> {
    Flux<TutorAvailability> findByTutorId(Long tutorId);
    Mono<Void> deleteByTutorId(Long tutorId);
    Mono<Void> deleteByTutorIdAndId(Long tutorId, Long id);

    @Query("SELECT * FROM tutor_availabilities WHERE tutor_id = :tutorId AND day_of_week = :dayOfWeek AND is_available = true")
    Flux<TutorAvailability> findByTutorIdAndDayOfWeek(
            @Param("tutorId") Long tutorId,
            @Param("dayOfWeek") String dayOfWeek
    );
}
