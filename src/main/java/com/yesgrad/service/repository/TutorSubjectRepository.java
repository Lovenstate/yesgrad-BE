package com.yesgrad.service.repository;

import com.yesgrad.service.domain.TutorSubject;
import com.yesgrad.service.dto.TutorSubjectResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TutorSubjectRepository extends ReactiveCrudRepository<TutorSubject, Long> {
    Flux<TutorSubject> findByTutorId(Long tutorId);

    Mono<Void> deleteByTutorId(Long tutorId);

    Flux<TutorSubject> findBySubjectId(Long subjectId);

    @Query("""
                    SELECT s.id,
                           s.tutor_id,
                    	   sub.name  AS subject_name,
                           s.subject_id,
                    	   s.hourly_rate,
                    	   s.created_at
                            FROM tutor_subjects s
                            JOIN subjects sub ON s.subject_id = sub.id
                            WHERE s.tutor_id = :tutorId
                            GROUP BY s.id, sub.name
            """)
    Flux<TutorSubjectResponse> findTutorSubjectsByTutorId(Long tutorId);
}
