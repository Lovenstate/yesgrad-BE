package com.yesgrad.service.repository;

import com.yesgrad.service.domain.Session;
import com.yesgrad.service.dto.SessionResponse;
import com.yesgrad.service.dto.TutorStudentSummary;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SessionRepository extends ReactiveCrudRepository<Session, Long> {

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.tutor_id = :tutorId AND s.session_date >= :startDate
                ORDER BY s.session_date LIMIT :limit
            """)
    Flux<SessionResponse> findUpcomingLessonsByTutor(@Param("tutorId") Long tutorId, @Param("startDate") LocalDate startDate, @Param("limit") int limit);

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.student_id = :studentId AND s.session_date >= :startDate
                AND s.status IN ('SCHEDULED', 'CONFIRMED')
                ORDER BY s.session_date ASC
            """)
    Flux<SessionResponse> findUpcomingLessonsByStudentId(@Param("studentId") Long studentId, @Param("startDate") LocalDate startDate);

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.tutor_id = :tutorId AND s.status = 'COMPLETED'
                ORDER BY s.session_date DESC LIMIT :limit
            """)
    Flux<SessionResponse> findRecentCompletedLessons(@Param("tutorId") Long tutorId, @Param("limit") int limit);

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.student_id = :studentId AND s.status = 'COMPLETED'
                ORDER BY s.session_date DESC LIMIT :limit
            """)
    Flux<SessionResponse> findRecentCompletedLessonsByStudent(@Param("studentId") Long studentId, int limit);

    @Query("SELECT COUNT(*) FROM sessions WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<Long> countCompletedLessonsByTutor(Long tutorId);

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) / 60.0 FROM sessions WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<Double> getTotalHoursByTutor(Long tutorId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM sessions WHERE tutor_id = :tutorId AND status = 'COMPLETED'")
    Mono<BigDecimal> getTotalEarningsByTutor(Long tutorId);

    Flux<Session> findByTutorId(Long tutorId);

    Flux<Session> findByStudentId(Long studentId);

    Flux<Session> findByTutorIdAndStatus(Long tutorId, String status);

    Mono<Long> countByTutorIdAndStatus(Long tutorId, String status);

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.tutor_id = :tutorId
            """)
    Flux<SessionResponse> findByTutorIdWithDetails(@Param("tutorId") Long tutorId);

    @Query("""
                 SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.tutor_id = :tutorId AND s.status = :status
            """)
    Flux<SessionResponse> findByTutorIdAndStatusWithDetails(@Param("tutorId") Long tutorId, @Param("status") String status);

    @Query("""
                SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.student_id = :studentId
            """)
    Flux<SessionResponse> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("""
                 SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
                WHERE s.student_id = :studentId AND s.status = :status
            """)
    Flux<SessionResponse> findByStudentIdAndStatusWithDetails(@Param("studentId") Long studentId, @Param("status") String status);

    @Query("""
               SELECT s.id, s.tutor_id, s.student_id,
                       tu.first_name || ' ' || tu.last_name AS tutor_name,
                       su.first_name || ' ' || su.last_name AS student_name,
                       s.subject_id, sub.name AS subject_name,
                       s.session_date, s.start_time, s.end_time,
                       s.duration_minutes, s.hourly_rate, s.amount,
                       s.lesson_format, s.location, s.status,
                       s.notes, s.cancellation_reason, s.created_at
                FROM sessions s
                JOIN tutor_profiles tp ON s.tutor_id = tp.id
                JOIN users tu ON tp.user_id = tu.id
                JOIN student_profiles sp ON s.student_id = sp.id
                JOIN users su ON sp.user_id = su.id
                JOIN subjects sub ON s.subject_id = sub.id
               WHERE s.id = :sessionId
            """)
    Mono<SessionResponse> findByIdWithDetails(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(*) FROM sessions WHERE student_id = :studentId")
    Mono<Long> countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(DISTINCT tutor_id) FROM sessions WHERE student_id = :studentId AND status = 'COMPLETED'")
    Mono<Long> countDistinctTutorsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) / 60.0 FROM sessions WHERE student_id = :studentId AND status = 'COMPLETED'")
    Mono<Double> getTotalHoursByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(*) FROM sessions WHERE student_id = :studentId AND status = 'COMPLETED'")
    Mono<Long> countCompletedSessionsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(DISTINCT tutor_id) FROM sessions WHERE student_id = :studentId AND status IN ('CONFIRMED', 'COMPLETED')")
    Mono<Long> countActiveTutorsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT * FROM sessions WHERE tutor_id = :tutorId AND session_date = :date AND status IN ('SCHEDULED', 'CONFIRMED')")
    Flux<Session> findBookedSlotsByTutorAndDate(@Param("tutorId") Long tutorId, @Param("date") LocalDate date);

    @Query("""
    SELECT COALESCE(SUM(duration_minutes) / 60.0, 0)
    FROM sessions
    WHERE student_id = :studentId AND status = 'COMPLETED'
    """)
    Mono<Double> sumCompletedHoursByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT
            u.id AS user_id,
            u.first_name || ' ' || u.last_name AS name,
            u.email,
            COUNT(s.id) AS total_sessions,
            COALESCE(SUM(s.duration_minutes) / 60, 0) AS total_hours,
            MAX(s.session_date)::TEXT AS last_session_at,
            STRING_AGG(DISTINCT sub.name, ',') AS subjects_taught
        FROM sessions s
        JOIN student_profiles sp ON s.student_id = sp.id
        JOIN users u ON sp.user_id = u.id
        JOIN subjects sub ON s.subject_id = sub.id
        WHERE s.tutor_id = :tutorId AND s.status = 'COMPLETED'
        GROUP BY u.id, u.first_name, u.last_name, u.email
    """)
    Flux<TutorStudentSummary> findStudentsByTutorId(@Param("tutorId") Long tutorId);
}
