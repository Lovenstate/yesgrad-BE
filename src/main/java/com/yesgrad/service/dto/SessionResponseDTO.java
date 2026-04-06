package com.yesgrad.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDTO {
    private Long id;
    private Long tutorId;
    private Long studentId;
    private String tutorName;
    private String subject;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private BigDecimal amount;
    private BigDecimal hourlyRate;
    private String lessonFormat;
    private String location;
    private String status;
    private String notes;
    private String cancellationReason;
    private LocalDateTime createdAt;
}
