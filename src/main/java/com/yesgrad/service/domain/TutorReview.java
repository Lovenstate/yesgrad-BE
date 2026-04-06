package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("reviews")
@Data
public class TutorReview {
    @Id
    private Long id;

    @Column("session_id")
    private Long sessionId;

    @Column("reviewer_id")
    private Long studentId;

    @Column("reviewee_id")
    private Long tutorId;
    private Integer rating;
    private String comment;
    @Column("is_public")
    private boolean isPublic;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
