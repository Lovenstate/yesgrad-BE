package com.yesgrad.service.repository;

import com.yesgrad.service.domain.StudentSubject;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentSubjectRepository extends ReactiveCrudRepository<StudentSubject, Long> {
    Flux<StudentSubject> findByStudentId(Long studentId);
    Mono<Void> deleteByStudentId(Long studentId);
}
