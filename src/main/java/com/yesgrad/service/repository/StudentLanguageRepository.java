package com.yesgrad.service.repository;

import com.yesgrad.service.domain.StudentLanguage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentLanguageRepository extends ReactiveCrudRepository<StudentLanguage, Long> {
    Flux<StudentLanguage> findByStudentId(Long studentId);
    Mono<Void> deleteByStudentId(Long studentId);
}
