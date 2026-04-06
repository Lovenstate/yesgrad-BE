package com.yesgrad.service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record StudentProfileRequest(
         String gradeLevel,
         String learningGoals,
         BigDecimal budgetMin,
         BigDecimal budgetMax,
         StudentProfile.LessonFormat lessonFormat,
         List<Integer> subjectIds,
        Boolean onboardingCompleted
) {
}
