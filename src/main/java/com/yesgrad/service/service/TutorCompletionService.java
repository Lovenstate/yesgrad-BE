package com.yesgrad.service.service;

import com.yesgrad.service.domain.TutorProfile;
import com.yesgrad.service.dto.OnboardingStatusResponse;
import com.yesgrad.service.repository.TutorAvailabilityRepository;
import com.yesgrad.service.repository.TutorEducationRepository;
import com.yesgrad.service.repository.TutorSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorCompletionService {

    private final TutorProfileService tutorProfileService;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final TutorEducationRepository tutorEducationRepository;
    private final TutorAvailabilityRepository tutorAvailabilityRepository;

    public Mono<OnboardingStatusResponse> calculate(Long tutorId) {
        Mono<TutorProfile> tutorProfile = tutorProfileService.getTutorProfile(tutorId);
        Mono<Boolean> hasSubjects = tutorSubjectRepository.findByTutorId(tutorId).hasElements();
        Mono<Boolean> hasEducation = tutorEducationRepository.findByTutorId(tutorId).hasElements();
        Mono<Boolean> hasAvailability = tutorAvailabilityRepository.findByTutorId(tutorId).hasElements();
        // TODO:: ADDS PAYOUT

        return Mono.zip(tutorProfile, hasSubjects, hasEducation, hasAvailability)
                .map(tuple ->{
                    TutorProfile tutor = tuple.getT1();
                    boolean subjects = tuple.getT2();
                    boolean education = tuple.getT3();
                    boolean availability = tuple.getT4();

                    int completion = 0;

                    List<String> completed = new ArrayList<>();
                    List<String> missing = new ArrayList<>();

                    if (hasNecessary(tutor)) {
                        completion += 30;
                        completed.add("PROFILE");
                    } else {
                        missing.add("PROFILE");
                    }

                    if (subjects) {
                        completion += 20;
                        completed.add("SUBJECT");
                    } else {
                        missing.add("SUBJECT");
                    }

                    if (education) {
                        completion += 20;
                        completed.add("EDUCATION");
                    } else {
                        missing.add("EDUCATION");
                    }

                    if (availability) {
                        completion += 20;
                        completed.add("AVAILABILITY");
                    } else {
                        missing.add("AVAILABILITY");
                    }

                    missing.add("PAYOUT");

                    String status = completion >= 90 ? "COMPLETED" : "STARTED";

                    return new OnboardingStatusResponse(completion, status, completed, missing);
                });

    }

    private boolean hasNecessary(TutorProfile tutorProfile) {
        return tutorProfile.getHeadline() != null
                && !tutorProfile.getHeadline().isBlank()
                && tutorProfile.getBio() != null
                && !tutorProfile.getBio().isBlank()
                && tutorProfile.getProfilePhotoUrl() != null
                && !tutorProfile.getProfilePhotoUrl().isBlank();
    }

    /**
     *
     * @param tutorId
     * @return TutorProfile
     * Must be called when
     * Tutor updates profile, Tutor adds/removes subjects, Tutor adds education
     * Tutor adds education, Tutor configures payout (later)
     */
    public Mono<TutorProfile> updateTutorCompletion(Long tutorId) {
        return calculate(tutorId)
                .flatMap(status ->
                        tutorProfileService.getTutorProfile(tutorId)
                                .flatMap(tutor -> {
                                    tutor.setProfileCompletion(status.profileCompletion());
                                    tutor.setOnboardingStatus(status.onboardingStatus());
                                    tutor.setUpdatedAt(LocalDateTime.now());
                                    return tutorProfileService.saveTutorProfile(tutor);
                                })
                );
    }

}
