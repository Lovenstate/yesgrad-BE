package com.yesgrad.service.dto;

public record TutorStudentSummary(
        Long userId,
        String name,
        String email,
        Long totalSessions,
        Long totalHours,
        String lastSessionAt,
        String subjectsTaught
) {}
