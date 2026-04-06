package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorProfile;
import com.yesgrad.service.dto.TutorSearchResult;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TutorProfileRepository extends ReactiveCrudRepository<TutorProfile, Long> {
    Mono<TutorProfile> findByUserId(Long userId);

    @Query("""
                SELECT tp.id AS tutor_id, tp.user_id,
                       u.first_name || ' ' || u.last_name AS name,
                       tp.headline, tp.bio, tp.profile_photo_url,
                       tp.instant_book,
                       MIN(ts.hourly_rate) AS hourly_rate,
                       COALESCE(AVG(r.rating), 0) AS rating,
                       COUNT(DISTINCT r.id) AS rating_count,
                       COUNT(DISTINCT s.id) AS total_sessions,
                       array_agg(DISTINCT sub.name) AS subjects
                FROM tutor_profiles tp
                JOIN users u ON tp.user_id = u.id
                JOIN tutor_subjects ts ON ts.tutor_id = tp.id
                JOIN subjects sub ON sub.id = ts.subject_id
                LEFT JOIN ratings r ON r.tutor_id = tp.id
                LEFT JOIN sessions s ON s.tutor_id = tp.id AND s.status = 'COMPLETED'
                WHERE (:search IS NULL OR LOWER(u.first_name || ' ' || u.last_name) LIKE LOWER('%' || :search || '%')
                       OR LOWER(sub.name) LIKE LOWER('%' || :search || '%'))
                  AND (:subjectId IS NULL OR ts.subject_id = :subjectId)
                  AND (:minPrice IS NULL OR ts.hourly_rate >= :minPrice)
                  AND (:maxPrice IS NULL OR ts.hourly_rate <= :maxPrice)
                GROUP BY tp.id, u.id
                ORDER BY
                  CASE WHEN :sortBy = 'price_asc' THEN MIN(ts.hourly_rate) END ASC NULLS LAST,
                  CASE WHEN :sortBy = 'price_desc' THEN MIN(ts.hourly_rate) END DESC NULLS LAST,
                  CASE WHEN :sortBy = 'experience' THEN COUNT(DISTINCT s.id) END DESC NULLS LAST,
                  COALESCE(AVG(r.rating), 0) DESC
            """)
    Flux<TutorSearchResult> searchTutors(
            @Param("search") String search,
            @Param("subjectId") Long subjectId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("sortBy") String sortBy
    );
}
