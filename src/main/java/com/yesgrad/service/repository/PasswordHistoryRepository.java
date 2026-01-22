package com.yesgrad.service.repository;

import com.yesgrad.service.domain.PasswordHistory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PasswordHistoryRepository extends ReactiveCrudRepository<PasswordHistory, Long> {
    
    @Query("SELECT * FROM password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<PasswordHistory> findRecentByUserId(Long userId, int limit);
}
