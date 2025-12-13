package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorLanguage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorLanguageRepository extends ReactiveCrudRepository<TutorLanguage, Long> {
    Flux<TutorLanguage> findByTutorId(Long tutorId);
    Mono<Void> deleteByTutorId(Long tutorId);
}
