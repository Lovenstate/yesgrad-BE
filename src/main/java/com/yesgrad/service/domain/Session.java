package com.yesgrad.service.domain;


import com.yesgrad.service.enums.LessonStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Table(name = "sessions")
@Data
public class Session {

    @Id
    private Long id;
    private Long tutorId;
    private Long studentId;
    private Long subjectId;
    private LocalDate sessionDate ;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private BigDecimal hourlyRate;
    private String lessonFormat; // ONLINE, IN_PERSON
    private String location;
    private BigDecimal amount;
    private LessonStatus status = LessonStatus.SCHEDULED;
    private String notes;
    private String cancellationReason;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
