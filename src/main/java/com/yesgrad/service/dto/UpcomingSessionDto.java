package com.yesgrad.service.dto;

import java.time.LocalDateTime;

public record UpcomingSessionDto(
        Long sessionId,
        String studentName,
        String subjectName,
        LocalDateTime startTime,
        String status
) {
}
