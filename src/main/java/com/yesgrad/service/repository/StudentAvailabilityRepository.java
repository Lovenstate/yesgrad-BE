package com.yesgrad.service.repository;

import com.yesgrad.service.domain.StudentAvailability;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentAvailabilityRepository extends ReactiveCrudRepository<StudentAvailability, Long> {
    Flux<StudentAvailability> findByStudentId(Long studentId);
    Mono<Void> deleteByStudentId(Long studentId);
}
