package com.yesgrad.service.domain;

import java.math.BigDecimal;

public record TutorSubjectRequest(
        Long subjectId,
        BigDecimal hourlyRate
) {
}
