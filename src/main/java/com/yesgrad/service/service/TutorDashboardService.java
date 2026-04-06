package com.yesgrad.service.service;

import com.yesgrad.service.domain.TutorProfile;
import com.yesgrad.service.domain.User;
import com.yesgrad.service.dto.SessionResponse;
import com.yesgrad.service.dto.TutorDashboardResponse;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorDashboardService {
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final TutorProfileRepository tutorProfileRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Cacheable(value = "tutorDashboard", key = "#userId", unless = "#result == null")
    public Mono<TutorDashboardResponse> getDashboardData(Long userId) {
        return tutorProfileRepository.findByUserId(userId)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(e -> {
                    log.error("Failed to fetch tutor profile for userId: {}", userId, e);
                    return Mono.empty();
                }).flatMap(tutor -> buildDashboardResponse(userId, tutor)
                        .map(dashboardComponents -> mapToResponse(dashboardComponents, tutor)));
    }

    private Mono<DashboardComponents> buildDashboardResponse(Long userId, TutorProfile tutor) {
        Mono<User> userMono = safeFetch(userRepository.findById(userId), new User(), "user");

        LocalDateTime since60Days = LocalDateTime.now().minusDays(60);

        Mono<Double> hoursMono = safeFetch(sessionRepository.getTotalHoursByTutor(tutor.getId()), 0.0, "Hours");
        Mono<Double> ratingMono = safeFetch(ratingRepository.getAverageRatingByTutor(tutor.getId()), 0.0, "Ratings");
        Mono<Long> ratingCountMono = safeFetch(ratingRepository.countRatingsByTutor(tutor.getId()), 0L, "RatingCount");
        Mono<BigDecimal> earningsMono = safeFetch(sessionRepository.getTotalEarningsByTutor(tutor.getId()), BigDecimal.ZERO, "Earnings");
        Mono<Long> unreadMessagesMono = safeFetch(messageRepository.countUnreadMessages(userId), 0L, "unreadMessages");
        Mono<List<SessionResponse>> upcomingLessonsMono = safeFetch(sessionRepository.findUpcomingLessonsByTutor(tutor.getId(), LocalDate.now(), 5).collectList(), List.of(), "UpcomingLessons");
        Mono<List<SessionResponse>> recentLessonsMono = safeFetch(sessionRepository.findRecentCompletedLessons(tutor.getId(), 5).collectList(), List.of(), "RecentLessons");
        Mono<Double> responseRateMono = safeFetch(messageRepository.getResponseRate(userId, since60Days), 0.0, "ResponseRate");
        Mono<Double> responseTimeMono = safeFetch(messageRepository.getAverageResponseTimeHours(userId, since60Days), 0.0, "ResponseTime");

        return Mono.zip(
                Mono.zip(userMono, hoursMono, ratingMono, ratingCountMono, earningsMono, unreadMessagesMono, upcomingLessonsMono, recentLessonsMono),
                Mono.zip(responseRateMono, responseTimeMono))
                .map(tuple -> new DashboardComponents(
                        tuple.getT1().getT1(), tuple.getT1().getT2(), tuple.getT1().getT3(), tuple.getT1().getT4(),
                        tuple.getT1().getT5(), tuple.getT1().getT6(), tuple.getT1().getT7(), tuple.getT1().getT8(),
                        tuple.getT2().getT1(), tuple.getT2().getT2()
                ));
    }

    private <T> Mono<T> safeFetch(Mono<T> source, T defaultValue, String context) {
        return source.timeout(Duration.ofSeconds(3))
                .onErrorResume(e -> {
                    log.warn("Failed to fetch {} for dashboard (non-fatal)", context, e);
                    return Mono.just(defaultValue);
                })
                .defaultIfEmpty(defaultValue);
    }

    private String formatResponseRate(Double rate) {
        return rate != null ? String.format("%.0f%%", rate) : "N/A";
    }

    private String formatResponseTime(Double hours) {
        if (hours == null) return "N/A";
        if (hours < 1) return "< 1 hour";
        return String.format("%.1f hours", hours);
    }

    private TutorDashboardResponse mapToResponse(DashboardComponents c, TutorProfile tutor) {
        String fullName = (c.user().getFirstName() != null ? c.user().getFirstName() : "") + " "
                + (c.user().getLastName() != null ? c.user().getLastName() : "");

        return TutorDashboardResponse.builder()
                .name(fullName)
                .hoursTutored(c.hours())
                .rating(c.rating() != null ? BigDecimal.valueOf(c.rating()) : null)
                .ratingCount(c.ratingCount())
                .responseRate(formatResponseRate(c.responseRate()))
                .responseTime(formatResponseTime(c.responseTime()))
                .totalEarnings(c.earnings())
                .amountPaid(BigDecimal.ZERO)
                .amountOwed(c.earnings())
                .unreadMessages(c.unreadMessages())
                .profileCompletion(tutor.getProfileCompletion() != null ? tutor.getProfileCompletion() : 0)
                .onboardingStatus(tutor.getOnboardingStatus() != null ? tutor.getOnboardingStatus() : "STARTED")
                .upcomingLessons(c.upcomingLessons())
                .recentLessons(c.recentLessons())
                .build();
    }

    private record DashboardComponents(
            User user,
            Double hours,
            Double rating,
            Long ratingCount,
            BigDecimal earnings,
            Long unreadMessages,
            List<SessionResponse> upcomingLessons,
            List<SessionResponse> recentLessons,
            Double responseRate,
            Double responseTime
    ) {}
}
