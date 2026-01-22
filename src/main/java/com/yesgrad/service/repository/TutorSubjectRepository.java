package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorSubject;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorSubjectRepository extends ReactiveCrudRepository<TutorSubject, Long> {
    Flux<TutorSubject> findByTutorId(Long tutorId);
    Mono<Void> deleteByTutorId(Long tutorId);
    Flux<TutorSubject> findBySubjectIdAndLevelId(Long subjectId, Long levelId);
}
