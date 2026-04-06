package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorPayoutSettings;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TutorPayoutSettingsRepository extends ReactiveCrudRepository<TutorPayoutSettings, Long> {
    Mono<TutorPayoutSettings> findByTutorId(Long tutorId);
}
