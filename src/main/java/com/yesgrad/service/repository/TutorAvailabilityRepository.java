package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorAvailability;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorAvailabilityRepository extends ReactiveCrudRepository<TutorAvailability, Long> {
    Flux<TutorAvailability> findByTutorId(Long tutorId);
    Mono<Void> deleteByTutorId(Long tutorId);
}
