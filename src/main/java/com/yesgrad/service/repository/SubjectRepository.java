package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Subject;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubjectRepository extends ReactiveCrudRepository<Subject, Long> {
    
    Mono<Subject> findByName(String name);

    Flux<Subject> findByParentId(Long parentId);

    Flux<Subject> findByNameContainingIgnoreCase(String name);
}