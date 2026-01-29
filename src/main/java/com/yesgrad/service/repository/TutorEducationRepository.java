package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorEducation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorEducationRepository extends ReactiveCrudRepository<TutorEducation, Long> {
    Flux<TutorEducation> findByTutorId(Long tutorId);
    Mono<Void> deleteByTutorId(Long tutorId);
    Mono<Void> deleteByTutorIdAndId(Long tutorId, Long id);
}
