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
public class LessonDTO {
    private Long id;
    private String studentName;
    private String subject;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private BigDecimal amount;
    private String status;
    private String notes;
}
