package com.yesgrad.service.dto;

import java.time.LocalTime;

public record AvailabilitySlot(
        LocalTime startTime,
        LocalTime endTime,
        Boolean isAvailable
) {
}
