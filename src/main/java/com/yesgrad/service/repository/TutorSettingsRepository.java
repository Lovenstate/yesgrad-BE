package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorSettings;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TutorSettingsRepository extends ReactiveCrudRepository<TutorSettings, Long> {
    Mono<TutorSettings> findByUserId(Long userId);
}
