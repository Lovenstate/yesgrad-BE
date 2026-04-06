package com.yesgrad.service.domain;

import com.yesgrad.service.enums.UserRole;

public record LoginResponse(String token,
                            UserRole role,
                            Boolean firstLogin,
                            String onboardingStatus,
                            Integer profileCompletion,
                            Boolean emailVerified) {
}
