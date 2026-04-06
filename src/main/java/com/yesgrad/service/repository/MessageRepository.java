package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Message;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {

    // Conversation
    @Query("""
            SELECT * FROM messages
            WHERE (sender_id = :userA AND receiver_id = :userB)
               OR (sender_id = :userB AND receiver_id = :userA)
            ORDER BY sent_at ASC
            """)
    Flux<Message> findConversation(Long userA, Long userB);

    @Query("""
            SELECT * FROM messages
            WHERE (receiver_id = :userA AND sender_id = :userB)
               OR (receiver_id = :userB AND sender_id = :userA)
            ORDER BY sent_at DESC
            LIMIT 1
            """)
    Mono<Message> findLatestInConversation(Long userA, Long userB);

    // ── Replies

    /** All messages that are direct replies to a given message. */
    @Query("SELECT * FROM messages WHERE reply_to_message_id = :messageId ORDER BY sent_at ASC")
    Flux<Message> findRepliesByMessageId(Long messageId);

    // Partners
    @Query("""
            SELECT DISTINCT
                CASE WHEN sender_id = :userId THEN receiver_id
                     ELSE sender_id END AS partner_id
            FROM messages
            WHERE sender_id = :userId OR receiver_id = :userId
            """)
    Flux<Long> findConversationPartnerIds(Long userId);

    // Unread
    @Query("""
            SELECT * FROM messages
            WHERE receiver_id = :receiverId AND is_read = false
            ORDER BY sent_at ASC
            """)
    Flux<Message> findUnreadByReceiver(Long receiverId);

    @Query("""
            SELECT COUNT(*) FROM messages
            WHERE sender_id = :senderId AND receiver_id = :receiverId AND is_read = false
            """)
    Mono<Long> countUnreadBetween(Long senderId, Long receiverId);

    // Analytics
    @Query("SELECT COUNT(*) FROM messages WHERE receiver_id = :userId AND is_read = false")
    Mono<Long> countUnreadMessages(Long userId);

    @Query("SELECT AVG(EXTRACT(EPOCH FROM (read_at - sent_at))/3600) FROM messages " +
            "WHERE receiver_id = :userId AND read_at IS NOT NULL AND sent_at >= :since")
    Mono<Double> getAverageResponseTimeHours(Long userId, LocalDateTime since);

    @Query("SELECT COALESCE(COUNT(CASE WHEN read_at IS NOT NULL THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 0) " +
            "FROM messages WHERE receiver_id = :userId AND sent_at >= :since")
    Mono<Double> getResponseRate(Long userId, LocalDateTime since);
}
