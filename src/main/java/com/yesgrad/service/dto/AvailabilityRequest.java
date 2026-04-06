package com.yesgrad.service.dto;

import java.time.LocalTime;

public record AvailabilityRequest(
         String dayOfWeek,
         LocalTime startTime,
         LocalTime endTime,
         Boolean isAvailable
) {
}