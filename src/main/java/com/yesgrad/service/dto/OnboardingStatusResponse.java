package com.yesgrad.service.dto;

import java.util.List;

public record OnboardingStatusResponse(
        int profileCompletion,
        String onboardingStatus,
        List<String> completedSteps,
        List<String> missingSteps
) {
}