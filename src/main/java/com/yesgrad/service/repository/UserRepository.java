package com.yesgrad.service.repository;

import com.yesgrad.service.domain.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    
    Mono<User> findByEmail(String email);
    
    Mono<Boolean> existsByEmail(String email);

    Mono<User> findByResetToken(String token);
}