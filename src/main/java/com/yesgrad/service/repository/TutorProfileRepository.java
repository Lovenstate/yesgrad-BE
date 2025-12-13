package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TutorProfileRepository extends ReactiveCrudRepository<TutorProfile, Long> {
    Mono<TutorProfile> findByUserId(Long userId);
}
