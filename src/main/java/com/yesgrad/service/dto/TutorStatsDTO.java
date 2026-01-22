package com.yesgrad.service.dto;

import java.math.BigDecimal;

public class TutorStatsDTO {
    private Long totalLessons;
    private Long completedLessons;
    private BigDecimal totalEarnings;
    private BigDecimal averageRating;
    private Long totalRatings;
    private String responseRate;
    private Integer averageResponseTimeHours;
}
