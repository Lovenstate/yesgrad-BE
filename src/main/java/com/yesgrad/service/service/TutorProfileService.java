package com.yesgrad.service.service;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.domain.Session;
import com.yesgrad.service.dto.*;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorProfileService {

    private final TutorProfileRepository tutorProfileRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final TutorLanguageRepository tutorLanguageRepository;
    private final TutorAvailabilityRepository tutorAvailabilityRepository;
    private final TransactionalOperator transactionalOperator;
    private final TutorSettingsRepository tutorSettingsRepository;
    private final TutorCompletionService tutorCompletionService;
    private final SessionRepository sessionRepository;

    @Transactional
    public Mono<TutorProfile> updateProfile(Long userId, TutorProfileRequest request) {
        return transactionalOperator.transactional(
                tutorProfileRepository.findByUserId(userId)
                        .switchIfEmpty(
                                createNewProfile(userId)
                                        .flatMap(tutorProfileRepository::save)
                        )
                        .flatMap(profile -> {
                            applyProfileUpdates(profile, request);
                            return tutorProfileRepository.save(profile);
                        })
        );
    }

    @Transactional
    public Mono<TutorSettingsResponse> updateSettings(Long userId, TutorSettingsRequest request) {
        return tutorSettingsRepository.findByUserId(userId)
                .flatMap(settings -> {
                    if (request.getResponseTime() != null) settings.setResponseTime(request.getResponseTime());
                    if (request.getEmailNotifications() != null) settings.setEmailNotifications(request.getEmailNotifications());
                    if (request.getSmsNotifications() != null) settings.setSmsNotifications(request.getSmsNotifications());
                    if (request.getLessonReminders() != null) settings.setLessonReminders(request.getLessonReminders());
                    return tutorSettingsRepository.save(settings);
                })
                .map(this::mapToResponse);
    }

    // need to see how to change that so education can be called
    private void applyProfileUpdates(TutorProfile profile, TutorProfileRequest request) {
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getCancellationPolicy() != null) profile.setCancellationPolicy(request.getCancellationPolicy());
        if (request.getTravelPolicy() != null) profile.setTravelPolicy(request.getTravelPolicy());
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private Mono<TutorProfile> createNewProfile(Long userId) {
        TutorProfile profile = new TutorProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        return Mono.just(profile);
    }

    // to be removed, call should be made to all services to bring data to create dashboard data
    // subjects, education
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

    public Mono<List<TutorSubject>> findTutorSubjects(Long subjectId) {
        return  tutorSubjectRepository.findBySubjectId(subjectId).collectList();
    }

    public Mono<List<TutorSubject>> findTutorSubjectsByTutorId(Long tutorId) {
        return  tutorSubjectRepository.findByTutorId(tutorId).collectList();
    }

    private Mono<Void> updateSubjects(Long tutorId, List<String> subjectNames) {
        return tutorSubjectRepository.deleteByTutorId(tutorId)
                .thenMany(reactor.core.publisher.Flux.fromIterable(subjectNames)
                        .flatMap(subjectRepository::findByName)
                        .map(subject -> {
                            TutorSubject ts = new TutorSubject();
                            ts.setTutorId(tutorId);
                            ts.setSubjectId(subject.getId());
                            return ts;
                        })
                        .flatMap(tutorSubjectRepository::save)
                ).then();
    }

    private Mono<Void> updateLanguages(Long tutorId, List<LanguageDto> languages) {
        return tutorLanguageRepository.deleteByTutorId(tutorId)
                .thenMany(reactor.core.publisher.Flux.fromIterable(languages)
                        .map(lang -> {
                            TutorLanguage tl = new TutorLanguage();
                            tl.setTutorId(tutorId);
                            tl.setLanguage(lang.getLanguage());
                            tl.setProficiency(lang.getProficiency());
                            return tl;
                        })
                        .flatMap(tutorLanguageRepository::save)
                ).then();
    }

    private Mono<Void> updateAvailability(Long tutorId, List<AvailabilityDto> availability) {
        return tutorAvailabilityRepository.deleteByTutorId(tutorId)
                .thenMany(reactor.core.publisher.Flux.fromIterable(availability)
                        .map(avail -> {
                            TutorAvailability ta = new TutorAvailability();
                            ta.setTutorId(tutorId);
                            ta.setDayOfWeek(avail.getDayOfWeek());
                            ta.setStartTime(LocalTime.parse(avail.getStartTime()));
                            ta.setEndTime(LocalTime.parse(avail.getEndTime()));
                            ta.setIsAvailable(avail.getIsAvailable());
                            return ta;
                        })
                        .flatMap(tutorAvailabilityRepository::save)
                ).then();
    }

    public Mono<TutorProfile> getProfile(Long userId) {
        return tutorProfileRepository.findByUserId(userId);
    }

    public Mono<TutorProfile> getTutorProfile(Long tutorId) {
        return tutorProfileRepository.findById(tutorId);
    }

    public Mono<TutorProfile> saveTutorProfile(TutorProfile tutorProfile) {
        return tutorProfileRepository.save(tutorProfile);
    }
    
    public Mono<Void> updateProfilePhoto(Long userId, String photoUrl) {
        return tutorProfileRepository.findByUserId(userId)
                .flatMap(profile -> {
                    String oldPhotoUrl = profile.getProfilePhotoUrl();
                    profile.setProfilePhotoUrl(photoUrl);
                    profile.setUpdatedAt(LocalDateTime.now());
                    return tutorProfileRepository.save(profile)
                            .doOnSuccess(p -> {
                                if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                                    deleteOldPhoto(oldPhotoUrl);
                                }
                            });
                })
                .then();
    }
    
    private void deleteOldPhoto(String photoUrl) {
        try {
            String filename = photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/profiles", filename);
            java.nio.file.Files.deleteIfExists(path);
        } catch (Exception e) {
            // Log but don't fail if deletion fails
        }
    }

    public Mono<TutorSettingsResponse> getTutorSettings(Long userId) {
        return tutorSettingsRepository.findByUserId(userId)
                .map(this::mapToResponse);
    }

    private TutorSettingsResponse mapToResponse(TutorSettings settings) {
        TutorSettingsResponse response = new TutorSettingsResponse();
        response.setId(settings.getId());
        response.setUserId(settings.getUserId());
        response.setResponseTime(settings.getResponseTime());
        response.setEmailNotifications(settings.isEmailNotifications());
        response.setSmsNotifications(settings.isSmsNotifications());
        response.setLessonReminders(settings.isLessonReminders());
        return response;
    }

    public Mono<Void> addTutorSubjects(Long userId, TutorSubjectRequestDto requestDto) {
        return tutorProfileRepository.findByUserId(userId)
                .flatMapMany(profile -> Flux.fromIterable(requestDto.subjects())
                        .map(request -> {
                            TutorSubject tutorSubject = new TutorSubject();
                            tutorSubject.setTutorId(profile.getId());
                            tutorSubject.setSubjectId(request.subjectId());
                            tutorSubject.setHourlyRate(request.hourlyRate());
                            return tutorSubject;
                        })
                        .flatMap(tutorSubjectRepository::save)
                        .then(tutorCompletionService.updateTutorCompletion(profile.getId()))
                )
                .then();
    }

    // dto that will return all tutor data for me, need to adjust
    public Mono<TutorProfileResponse> getProfileWithDetails(Long userId) {
        return tutorProfileRepository.findByUserId(userId)
                .flatMap(profile -> {

                    Mono<List<String>> subjectsMono =
                            tutorSubjectRepository.findByTutorId(profile.getId())
                                    .flatMap(ts -> subjectRepository.findById(ts.getSubjectId()))
                                    .map(Subject::getName)
                                    .collectList();

                    Mono<List<LanguageDto>> languagesMono =
                            tutorLanguageRepository.findByTutorId(profile.getId())
                                    .map(lang -> {
                                        LanguageDto dto = new LanguageDto();
                                        dto.setLanguage(lang.getLanguage());
                                        dto.setProficiency(lang.getProficiency());
                                        return dto;
                                    })
                                    .collectList();

                    Mono<List<AvailabilityDto>> availabilityMono =
                            tutorAvailabilityRepository.findByTutorId(profile.getId())
                                    .map(av -> {
                                        AvailabilityDto dto = new AvailabilityDto();
                                        dto.setDayOfWeek(av.getDayOfWeek());
                                        dto.setStartTime(av.getStartTime().toString());
                                        dto.setEndTime(av.getEndTime().toString());
                                        dto.setIsAvailable(av.getIsAvailable());
                                        return dto;
                                    })
                                    .collectList();

                    return Mono.zip(subjectsMono, languagesMono, availabilityMono)
                            .map(tuple -> {
                                TutorProfileResponse response = new TutorProfileResponse();

                                response.setId(profile.getId());
                                response.setUserId(profile.getUserId());
                                response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
                                response.setBio(profile.getBio());
                                response.setInstantBook(profile.getInstantBook());
                                response.setCancellationPolicy(profile.getCancellationPolicy());
                                response.setTravelPolicy(profile.getTravelPolicy());
                                response.setCreatedAt(profile.getCreatedAt());
                                response.setUpdatedAt(profile.getUpdatedAt());
                                response.setHeadline(profile.getHeadline());
                                response.setOnboardingStatus(profile.getOnboardingStatus());
                                response.setProfileCompletion(profile.getProfileCompletion());

                                response.setSubjects(tuple.getT1());
                                response.setLanguages(tuple.getT2());
                                response.setAvailability(tuple.getT3());

                                return response;
                            });
                });
    }

    public Mono<List<AvailableSlot>> getAvailableSlots(Long tutorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return Mono.just(List.of());
        }

        String dayOfWeek = date.getDayOfWeek().name();

        return Mono.zip(
                sessionRepository.findBookedSlotsByTutorAndDate(tutorId, date).collectList(),
                tutorAvailabilityRepository.findByTutorIdAndDayOfWeek(tutorId, dayOfWeek).collectList()
        ).map(tuple -> {
            List<Session> booked = tuple.getT1();
            List<TutorAvailability> availabilities = tuple.getT2();

            List<AvailableSlot> slots = new ArrayList<>();
            for (TutorAvailability availability : availabilities) {
                LocalTime cursor = availability.getStartTime();
                LocalTime end = availability.getEndTime();

                while (!cursor.plusHours(1).isAfter(end)) {
                    LocalTime slotStart = cursor;
                    LocalTime slotEnd = cursor.plusHours(1);

                    boolean isBooked = booked.stream().anyMatch(s ->
                            s.getStartTime().isBefore(slotEnd) && s.getEndTime().isAfter(slotStart)
                    );

                    if (!isBooked) {
                        slots.add(new AvailableSlot(date, slotStart, slotEnd));
                    }
                    cursor = slotEnd;
                }
            }
            return slots;
        });
    }

    public Mono<List<TutorStudentResponse>> getTutorStudents(Long tutorId) {
        return sessionRepository.findStudentsByTutorId(tutorId)
                .map(summary -> new TutorStudentResponse(
                        summary.userId(),
                        summary.name(),
                        summary.email(),
                        summary.totalSessions(),
                        summary.totalHours(),
                        summary.lastSessionAt(),
                        summary.subjectsTaught() != null ? summary.subjectsTaught().split(",") : new String[]{}
                ))
                .collectList();
    }

    public Mono<List<TutorSubjectResponse>> getTutorSubjects(Long tutorId) {
        return tutorSubjectRepository.findTutorSubjectsByTutorId(tutorId)
                .collectList();
    }

    public Mono<List<TutorSearchResult>> searchTutors(
            String search, Long subjectId, BigDecimal minPrice,
            BigDecimal maxPrice, String sortBy
    ) {
        return tutorProfileRepository.searchTutors(search, subjectId, minPrice, maxPrice, sortBy)
                .collectList();
    }

}
