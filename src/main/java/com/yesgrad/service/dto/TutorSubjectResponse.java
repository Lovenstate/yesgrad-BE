package com.yesgrad.service.dto;

import java.time.LocalDateTime;

public record TutorSubjectResponse(
        Long id,
        Long tutorId,
        Long subjectId,
        String subjectName,
        Double hourlyRate,
        LocalDateTime createdAt
) {
}
