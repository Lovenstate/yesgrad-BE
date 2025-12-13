package com.yesgrad.service.service;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.repository.SubjectRepository;
import com.yesgrad.service.repository.TutorAvailabilityRepository;
import com.yesgrad.service.repository.TutorLanguageRepository;
import com.yesgrad.service.repository.TutorProfileRepository;
import com.yesgrad.service.repository.TutorSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Mono<TutorProfile> updateProfile(Long userId, TutorProfileRequest request) {
        return profileRepository.findByUserId(userId)
                .switchIfEmpty(createNewProfile(userId))
                .flatMap(profile -> {
                    profile.setSchool(request.getSchool());
                    profile.setDegree(request.getDegree());
                    profile.setFieldOfStudy(request.getFieldOfStudy());
                    profile.setGraduationYear(request.getGraduationYear());
                    profile.setHourlyRate(request.getHourlyRate());
                    profile.setCancellationPolicy(request.getCancellationPolicy());
                    profile.setTravelPolicy(request.getTravelPolicy());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return profileRepository.save(profile);
                })
                .flatMap(profile -> updateSubjects(profile.getId(), request.getSubjects())
                        .then(updateLanguages(profile.getId(), request.getLanguages()))
                        .then(updateAvailability(profile.getId(), request.getAvailability()))
                        .thenReturn(profile)
                );
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
                        .flatMap(name -> subjectRepo.findByName(name))
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
    
    public Mono<TutorProfileResponse> getProfileWithDetails(Long userId) {
        return profileRepository.findByUserId(userId)
                .flatMap(profile -> {
                    TutorProfileResponse response = new TutorProfileResponse();
                    response.setId(profile.getId());
                    response.setUserId(profile.getUserId());
                    response.setProfilePhotoUrl(profile.getProfilePhotoUrl());
                    response.setSchool(profile.getSchool());
                    response.setDegree(profile.getDegree());
                    response.setFieldOfStudy(profile.getFieldOfStudy());
                    response.setGraduationYear(profile.getGraduationYear());
                    response.setHourlyRate(profile.getHourlyRate());
                    response.setCancellationPolicy(profile.getCancellationPolicy());
                    response.setTravelPolicy(profile.getTravelPolicy());
                    response.setCreatedAt(profile.getCreatedAt());
                    response.setUpdatedAt(profile.getUpdatedAt());
                    
                    return subjectRepository.findByTutorId(profile.getId())
                            .flatMap(ts -> subjectRepo.findById(ts.getSubjectId()))
                            .map(Subject::getName)
                            .collectList()
                            .doOnNext(response::setSubjects)
                            .thenReturn(response);
                });
    }
}
