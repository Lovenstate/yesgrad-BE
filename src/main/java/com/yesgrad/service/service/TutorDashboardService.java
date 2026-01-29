package com.yesgrad.service.service;

import com.yesgrad.service.domain.Lesson;
import com.yesgrad.service.domain.TutorProfile;
import com.yesgrad.service.domain.TutorSubject;
import com.yesgrad.service.domain.TutorSubjectRequest;
import com.yesgrad.service.dto.LessonDTO;
import com.yesgrad.service.dto.TutorDashboardDTO;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorDashboardService {

    private final TutorProfileRepository tutorProfileRepository;
    private final LessonRepository lessonRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final TutorSubjectRepository tutorSubjectRepository;

    public Mono<TutorDashboardDTO> getDashboardData(Long userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    // Get all dashboard data in parallel

                    Mono<Long> hoursMono = lessonRepository.getTotalHoursByTutor(userId)
                            .defaultIfEmpty(0L)
                            .map(minutes -> minutes / 60);

                    Mono<Double> ratingMono = ratingRepository.getAverageRatingByTutor(userId)
                            .defaultIfEmpty(0.0);

                    Mono<Long> ratingCountMono = ratingRepository.countRatingsByTutor(userId)
                            .defaultIfEmpty(0L);

                    Mono<BigDecimal> earningsMono = lessonRepository.getTotalEarningsByTutor(userId)
                            .defaultIfEmpty(BigDecimal.ZERO);

                    Mono<Long> unreadMessagesMono = messageRepository.countUnreadMessages(userId)
                            .defaultIfEmpty(0L);

                    Mono<List<Lesson>> upcomingLessonsMono = lessonRepository
                            .findUpcomingLessonsByTutor(userId, LocalDateTime.now(), 5)
                            .collectList();

                    Mono<List<Lesson>> recentLessonsMono = lessonRepository
                            .findRecentCompletedLessons(userId, 5)
                            .collectList();

                    // Get response metrics for last 60 days
                    LocalDateTime since60Days = LocalDateTime.now().minusDays(60);

                    Mono<Double> responseRateMono = messageRepository.getResponseRate(userId, since60Days)
                            .defaultIfEmpty(0.0);

                    Mono<Double> responseTimeMono = messageRepository.getAverageResponseTimeHours(userId, since60Days)
                            .defaultIfEmpty(0.0);

                    return Mono.zip(
                                    Mono.zip(hoursMono, ratingMono, ratingCountMono, earningsMono, unreadMessagesMono, upcomingLessonsMono, recentLessonsMono),
                                    Mono.zip(responseRateMono, responseTimeMono))
                            .map(tuple -> {
                                Long hours = tuple.getT1().getT1();
                                Double rating = tuple.getT1().getT2();
                                Long ratingCount = tuple.getT1().getT3();
                                BigDecimal earnings = tuple.getT1().getT4();
                                Long unreadMessages = tuple.getT1().getT5();
                                List<Lesson> upcomingLessons = tuple.getT1().getT6();
                                List<Lesson> recentLessons = tuple.getT1().getT7();
                                Double responseRate = tuple.getT2().getT1();
                                Double responseTime = tuple.getT2().getT2();

                                return TutorDashboardDTO.builder()
                                        .name(user.getFirstName() + " " + user.getLastName())
                                        .hoursTutored(hours)
                                        .rating(rating != null ? BigDecimal.valueOf(rating) : null)
                                        .ratingCount(ratingCount)
                                        .responseRate(formatResponseRate(responseRate))
                                        .responseTime(formatResponseTime(responseTime))
                                        .totalEarnings(earnings)
                                        .amountPaid(BigDecimal.ZERO) // TODO: Calculate from payments
                                        .amountOwed(earnings) // TODO: Calculate pending payments
                                        .unreadMessages(unreadMessages)
                                        .upcomingLessons(mapToLessonDTOs(upcomingLessons))
                                        .recentLessons(mapToLessonDTOs(recentLessons))
                                        .build();
                            });
                });
    }

    public Mono<TutorSubject> addTutorSubject(Long userId, TutorSubjectRequest request) {
        return tutorProfileRepository.findByUserId(userId)
                .flatMap(tutor -> {
                    TutorSubject tutorSubject = new TutorSubject();
                    tutorSubject.setTutorId(tutor.getId());
                    tutorSubject.setSubjectId(request.subjectId());
                    tutorSubject.setHourlyRate(request.hourlyRate());
                    return tutorSubjectRepository.save(tutorSubject);
                });
    }

    public Flux<TutorSubject> findTutorSubjects(Long subjectId) {
        return  tutorSubjectRepository.findBySubjectId(subjectId);
    }

    private String formatResponseRate(Double rate) {
        return rate != null ? String.format("%.0f%%", rate) : "N/A";
    }

    private String formatResponseTime(Double hours) {
        if (hours == null) return "N/A";
        if (hours < 1) return "< 1 hour";
        return String.format("%.1f hours", hours);
    }

    private List<LessonDTO> mapToLessonDTOs(List<Lesson> lessons) {
        return lessons.stream()
                .map(lesson -> LessonDTO.builder()
                        .id(lesson.getId())
                        .subject(lesson.getSubject())
                        .scheduledAt(lesson.getScheduledAt())
                        .durationMinutes(lesson.getDurationMinutes())
                        .amount(lesson.getAmount())
                        .status(lesson.getStatus().toString())
                        .notes(lesson.getNotes())
                        .build())
                .collect(Collectors.toList());
    }

}
