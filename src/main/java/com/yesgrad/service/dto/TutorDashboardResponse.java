package com.yesgrad.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDashboardResponse {

    private String name;
    private Double hoursTutored;
    private BigDecimal rating;
    private Long ratingCount;
    private Boolean hasDirectDeposit;
    private String responseRate;
    private String responseTime;
    private BigDecimal totalEarnings;
    private BigDecimal amountPaid;
    private BigDecimal amountOwed;
    private Long unreadMessages;
    private List<SessionResponse> upcomingLessons;
    private List<SessionResponse> recentLessons;
    private Integer profileCompletion;
    private String  onboardingStatus;
}
