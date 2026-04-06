package com.yesgrad.service.dto;

import java.math.BigDecimal;

public record TutorSearchResult(
        Long tutorId,
        Long userId,
        String name,
        String headline,
        String bio,
        String profilePhotoUrl,
        BigDecimal hourlyRate,
        Boolean instantBook,
        Double rating,
        Long ratingCount,
        Long totalSessions,
        String[] subjects   // mapped from array_agg
) {
}
