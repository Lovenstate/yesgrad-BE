package com.yesgrad.service.repository;

import com.yesgrad.service.domain.PasswordResetAttempt;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PasswordResetAttemptRepository extends ReactiveCrudRepository<PasswordResetAttempt, Long> {
    
    @Query("SELECT COUNT(*) FROM password_reset_attempts WHERE email = :email AND attempted_at > :since")
    Mono<Long> countRecentAttempts(String email, LocalDateTime since);
}
