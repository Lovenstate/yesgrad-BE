package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Lesson;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LessonRepository extends ReactiveCrudRepository<Lesson, Long> {

    @Query("SELECT * FROM lessons WHERE tutor_id = :tutorId AND scheduled_at >= :startDate ORDER BY scheduled_at LIMIT :limit")
    Flux<Lesson> findUpcomingLessonsByTutor(Long tutorId, LocalDateTime startDate, int limit);

    @Query("SELECT * FROM lessons WHERE tutor_id = :tutorId AND status = 'COMPLETED' ORDER BY scheduled_at DESC LIMIT :limit")
    Flux<Lesson> findRecentCompletedLessons(Long tutorId, int limit);

    @Query("SELECT COUNT(*) FROM lessons WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<Long> countCompletedLessonsByTutor(Long tutorId);

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM lessons WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<Long> getTotalHoursByTutor(Long tutorId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM lessons WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<BigDecimal> getTotalEarningsByTutor(Long tutorId);
}
