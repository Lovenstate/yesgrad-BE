package com.yesgrad.service.service;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorProfileService {

    private final TutorProfileRepository profileRepository;
    private final TutorSubjectRepository subjectRepository;
    private final SubjectRepository subjectRepo;
    private final TutorLanguageRepository languageRepository;
    private final TutorAvailabilityRepository availabilityRepository;
    private final TransactionalOperator transactionalOperator;
    private final TutorSettingsRepository tutorSettingsRepository;

    @Transactional
    public Mono<TutorProfile> updateProfile(Long userId, TutorProfileRequest request) {
        return transactionalOperator.transactional(
                profileRepository.findByUserId(userId)
                        .switchIfEmpty(
                                createNewProfile(userId)
                                        .flatMap(profileRepository::save)
                        )
                        .flatMap(profile -> {
                            applyProfileUpdates(profile, request);
                            return profileRepository.save(profile);
                        })
                        .flatMap(profile ->
                                updateSubjects(profile.getId(), request.getSubjects())
                                        .then(updateLanguages(profile.getId(), request.getLanguages()))
                                        .then(updateAvailability(profile.getId(), request.getAvailability()))
                                        .thenReturn(profile)
                        )
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

    private void applyProfileUpdates(TutorProfile profile, TutorProfileRequest request) {
        profile.setSchool(request.getSchool());
        profile.setDegree(request.getDegree());
        profile.setFieldOfStudy(request.getFieldOfStudy());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setCancellationPolicy(request.getCancellationPolicy());
        profile.setTravelPolicy(request.getTravelPolicy());
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private Mono<TutorProfile> createNewProfile(Long userId) {
        TutorProfile profile = new TutorProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        return Mono.just(profile);
    }

    private Mono<Void> updateSubjects(Long tutorId, List<String> subjectNames) {
        return subjectRepository.deleteByTutorId(tutorId)
                .thenMany(reactor.core.publisher.Flux.fromIterable(subjectNames)
                        .flatMap(subjectRepo::findByName)
                        .map(subject -> {
                            TutorSubject ts = new TutorSubject();
                            ts.setTutorId(tutorId);
                            ts.setSubjectId(subject.getId());
                            return ts;
                        })
                        .flatMap(subjectRepository::save)
                ).then();
    }

    private Mono<Void> updateLanguages(Long tutorId, List<LanguageDto> languages) {
        return languageRepository.deleteByTutorId(tutorId)
                .thenMany(reactor.core.publisher.Flux.fromIterable(languages)
                        .map(lang -> {
                            TutorLanguage tl = new TutorLanguage();
                            tl.setTutorId(tutorId);
                            tl.setLanguage(lang.getLanguage());
                            tl.setProficiency(lang.getProficiency());
                            return tl;
                        })
                        .flatMap(languageRepository::save)
                ).then();
    }

    private Mono<Void> updateAvailability(Long tutorId, List<AvailabilityDto> availability) {
        return availabilityRepository.deleteByTutorId(tutorId)
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
                        .flatMap(availabilityRepository::save)
                ).then();
    }

    public Mono<TutorProfile> getProfile(Long userId) {
        return profileRepository.findByUserId(userId);
    }
    
    public Mono<Void> updateProfilePhoto(Long userId, String photoUrl) {
        return profileRepository.findByUserId(userId)
                .flatMap(profile -> {
                    String oldPhotoUrl = profile.getProfilePhotoUrl();
                    profile.setProfilePhotoUrl(photoUrl);
                    profile.setUpdatedAt(LocalDateTime.now());
                    return profileRepository.save(profile)
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

    public Mono<TutorProfileResponse> getProfileWithDetails(Long userId) {
        return profileRepository.findByUserId(userId)
                .flatMap(profile -> {

                    Mono<List<String>> subjectsMono =
                            subjectRepository.findByTutorId(profile.getId())
                                    .flatMap(ts -> subjectRepo.findById(ts.getSubjectId()))
                                    .map(Subject::getName)
                                    .collectList();

                    Mono<List<LanguageDto>> languagesMono =
                            languageRepository.findByTutorId(profile.getId())
                                    .map(lang -> {
                                        LanguageDto dto = new LanguageDto();
                                        dto.setLanguage(lang.getLanguage());
                                        dto.setProficiency(lang.getProficiency());
                                        return dto;
                                    })
                                    .collectList();

                    Mono<List<AvailabilityDto>> availabilityMono =
                            availabilityRepository.findByTutorId(profile.getId())
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
                                response.setSchool(profile.getSchool());
                                response.setDegree(profile.getDegree());
                                response.setFieldOfStudy(profile.getFieldOfStudy());
                                response.setGraduationYear(profile.getGraduationYear());
                                response.setHourlyRate(profile.getHourlyRate());
                                response.setCancellationPolicy(profile.getCancellationPolicy());
                                response.setTravelPolicy(profile.getTravelPolicy());
                                response.setCreatedAt(profile.getCreatedAt());
                                response.setUpdatedAt(profile.getUpdatedAt());

                                response.setSubjects(tuple.getT1());
                                response.setLanguages(tuple.getT2());
                                response.setAvailability(tuple.getT3());

                                return response;
                            });
                });
    }

}
