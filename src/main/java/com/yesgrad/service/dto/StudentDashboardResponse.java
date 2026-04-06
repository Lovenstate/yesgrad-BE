package com.yesgrad.service.dto;

import java.util.List;

public record StudentDashboardResponse(
        List<SessionResponse> upcomingSessions,
        List<SessionResponse> recentSessions,
        int totalSessions,
        int totalHours,
        int activeTutors,
        int unreadMessages,
        boolean hasPaymentMethod
) {
}
