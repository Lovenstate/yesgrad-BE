package com.yesgrad.service.domain;


import com.yesgrad.service.enums.LessonStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    private Long id;

    private Long tutorId;
    private Long studentId;

    private String subject;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private BigDecimal amount;

    private LessonStatus status = LessonStatus.SCHEDULED;

    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
