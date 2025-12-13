package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Subject;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubjectRepository extends R2dbcRepository<Subject, Long> {
    
    Flux<Subject> findByIsActiveTrue();
    
    Flux<Subject> findByCategory(String category);
    
    Mono<Subject> findByName(String name);
}