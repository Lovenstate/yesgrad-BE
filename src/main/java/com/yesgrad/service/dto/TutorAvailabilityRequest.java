package com.yesgrad.service.dto;

import java.util.List;

public record TutorAvailabilityRequest(
        List<AvailabilityRequest> availabilities
) {
}
