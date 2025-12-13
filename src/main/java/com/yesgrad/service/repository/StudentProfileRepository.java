package com.yesgrad.service.repository;

import com.yesgrad.service.domain.StudentProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StudentProfileRepository extends ReactiveCrudRepository<StudentProfile, Long> {
    Mono<StudentProfile> findByUserId(Long userId);
    Mono<Boolean> existsByUserId(Long userId);
}
