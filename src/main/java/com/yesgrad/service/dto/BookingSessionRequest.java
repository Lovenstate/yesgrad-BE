package com.yesgrad.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingSessionRequest(
        Long studentId,
        Long tutorId,
        Long subjectId,
        LocalDate sessionDate,
        Integer durationMinutes,
        BigDecimal hourlyRate,
        BigDecimal amount,
        LocalTime startTime,
        LocalTime endTime,
        String lessonFormat,
        String location,
        String notes
) {
}
