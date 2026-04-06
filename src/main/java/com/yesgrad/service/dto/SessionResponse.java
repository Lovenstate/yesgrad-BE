package com.yesgrad.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SessionResponse(
        Long id,
        Long studentId,
        String studentName,
        Long tutorId,
        String tutorName,
        Long subjectId,
        String subjectName,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes,
        BigDecimal hourlyRate,
        BigDecimal amount,
        String lessonFormat,
        String location,
        String status,
        String notes,
        String cancellationReason,
        Long cancelledBy,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
