package com.yesgrad.service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StudentProfileRequest(
        @NotNull List<String> subjects,
        @NotBlank String gradeLevel,
        String learningGoals,
        @NotBlank String budget,
        @NotBlank String lessonFormat
) {
}
