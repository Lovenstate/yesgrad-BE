package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Message;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {


    @Query("SELECT COUNT(*) FROM messages WHERE receiver_id = :userId AND is_read = false")
    Mono<Long> countUnreadMessages(Long userId);

    @Query("SELECT AVG(EXTRACT(EPOCH FROM (read_at - sent_at))/3600) FROM messages " +
            "WHERE receiver_id = :userId AND read_at IS NOT NULL AND sent_at >= :since")
    Mono<Double> getAverageResponseTimeHours(Long userId, LocalDateTime since);

    @Query("SELECT COUNT(CASE WHEN read_at IS NOT NULL THEN 1 END) * 100.0 / COUNT(*) " +
            "FROM messages WHERE receiver_id = :userId AND sent_at >= :since")
    Mono<Double> getResponseRate(Long userId, LocalDateTime since);
}
