package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.dto.*;
import com.yesgrad.service.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tutor/profile")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;
    private final FileStorageService fileStorageService;
    private final TutorSettingsService tutorSettingsService;
    private final TutorCompletionService tutorCompletionService;
    private final TutorEducationService tutorEducationService;
    private final TutorAvailabilityService tutorAvailabilityService;

    @GetMapping
    public Mono<CommonResponse<TutorProfileResponse>> getProfile(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.getProfileWithDetails(userId)
                .map(profile -> CommonResponse.success("Profile retrieved", profile))
                .defaultIfEmpty(CommonResponse.success("No profile found", null));
    }

    // create get endpoint to get tutor profile by userId

    @GetMapping("/settings")
    public Mono<CommonResponse<TutorSettingsResponse>> getTutorSettings(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.getTutorSettings(userId)
                .map(settings -> CommonResponse.success("Settings retrieved", settings))
                .defaultIfEmpty(CommonResponse.success("No profile with settings found", null));
    }

    @PutMapping("/settings")
    public Mono<CommonResponse<TutorSettingsResponse>> updateTutorSettings(
            Authentication authentication,
            @RequestBody TutorSettingsRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.updateSettings(userId, request)
                .map(profile -> CommonResponse.success("Tutor settings updated successfully", profile));
    }

    @GetMapping("/complete-settings")
    public Mono<CommonResponse<TutorCompleteSettingsResponse>> getCompleteSettings(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorSettingsService.getCompleteSettings(userId)
                .map(settings -> CommonResponse.success("Complete settings retrieved", settings));
    }

    @PutMapping("/complete-settings")
    public Mono<CommonResponse<TutorCompleteSettingsResponse>> updateCompleteSettings(
            Authentication authentication,
            @Valid @RequestBody TutorCompleteSettingsRequest request,
            ServerHttpResponse response) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorSettingsService.updateCompleteSettings(userId, request)
                .map(settings -> {
                    if (Boolean.TRUE.equals(settings.getPasswordChanged())) {
                        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(0)
                                .build();
                        response.addCookie(cookie);
                    }
                    return CommonResponse.success("Settings updated successfully", settings);
                });
    }

    @PutMapping
    public Mono<CommonResponse<TutorProfile>> updateProfile(
            Authentication authentication,
            @RequestBody TutorProfileRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.updateProfile(userId, request)
                .flatMap(tutorCompletionService::updateTutorCompletionWithProfile)
                .map(profile -> CommonResponse.success("Tutor profile updated successfully", profile));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<CommonResponse<String>> uploadPhoto(
            Authentication authentication,
            @RequestPart("file") FilePart filePart) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return fileStorageService.saveProfilePhoto(filePart)
                .flatMap(photoUrl -> tutorProfileService.updateProfilePhoto(userId, photoUrl)
                        .thenReturn(CommonResponse.success("Photo uploaded successfully", photoUrl))
                );
    }


    // Tutor Subject Endpoints
    @PostMapping("/subjects")
    public Mono<CommonResponse<Void>> addTutorSubject(Authentication authentication, @RequestBody TutorSubjectRequestDto request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.addTutorSubjects(userId, request)
                .map(subject -> CommonResponse.success("Subject added successfully", subject));
    }

    @GetMapping("/subjects/search/{subjectId}")
    public Mono<CommonResponse<List<TutorSubject>>> getAllTutorSubjects(@PathVariable Long subjectId) {
        return tutorProfileService.findTutorSubjects(subjectId)
                .map(CommonResponse::success);
    }

    @GetMapping("/subjects/tutor/{tutorId}")
    public Mono<CommonResponse<List<TutorSubject>>> getAllTutorSubjectsByTutor(@PathVariable Long tutorId) {
        return tutorProfileService.findTutorSubjectsByTutorId(tutorId)
                .map(CommonResponse::success);
    }

    // Tutor Education Endpoints
    @PostMapping("/{tutorId}/education")
    public Mono<CommonResponse<Void>> saveTutorEducation(@PathVariable Long tutorId,
                                                         @RequestBody TutorEducationRequest request) {
        return tutorEducationService.saveEducation(tutorId, request)
                .map(CommonResponse::success);
    }

    @GetMapping("/{tutorId}/education")
    public Mono<CommonResponse<List<TutorEducation>>> getTutorEducation(@PathVariable Long tutorId) {
        return tutorEducationService.getEducations(tutorId)
                .map(CommonResponse::success);
    }

    @DeleteMapping("/{tutorId}/id/{id}/education")
    public Mono<CommonResponse<Void>> deleteTutorEducation(@PathVariable Long tutorId, @PathVariable Long id) {
        return tutorEducationService.deleteEducation(tutorId, id)
                .map(CommonResponse::success);
    }

    // Tutor Availability Endpoints
    @PostMapping("/{tutorId}/availabilities")
    public Mono<CommonResponse<Void>> saveTutorAvailabilities(@PathVariable Long tutorId,
                                                              @RequestBody TutorAvailabilityRequest availabilityRequest) {

        return tutorAvailabilityService.saveAvailabilities(tutorId, availabilityRequest)
                .map(CommonResponse::success);
    }

    @PostMapping("/{tutorId}/availability")
    public Mono<CommonResponse<Void>> saveTutorAvailability(@PathVariable Long tutorId,
                                                            @RequestBody AvailabilityRequest availabilityRequest) {

        return tutorAvailabilityService.saveAvailability(tutorId, availabilityRequest)
                .map(CommonResponse::success);
    }

    @GetMapping("/{tutorId}/availability")
    public Mono<CommonResponse<List<TutorAvailability>>> getTutorAvailability(@PathVariable Long tutorId) {
        return tutorAvailabilityService.getAvailabilities(tutorId)
                .map(CommonResponse::success);
    }

    @DeleteMapping("/{tutorId}/id/{id}/availability")
    public Mono<CommonResponse<Void>> deleteTutorAvailability(@PathVariable Long tutorId, @PathVariable Long id) {
        return tutorAvailabilityService.deleteAvailability(tutorId, id)
                .map(CommonResponse::success);
    }

    @GetMapping("/search")
    public Mono<CommonResponse<List<TutorSearchResult>>> searchTutors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "rating") String sortBy
    ) {
        return tutorProfileService.searchTutors(search, subjectId, minPrice, maxPrice, sortBy)
                .map(results -> CommonResponse.success("Tutors retrieved", results));
    }


    @GetMapping("/slot/{tutorId}/availability")
    public Mono<CommonResponse<List<AvailableSlot>>> getTutorAvailability(
            @PathVariable Long tutorId,
            @RequestParam String date
    ) {
        return tutorProfileService.getAvailableSlots(tutorId, LocalDate.parse(date))
                .map(slots -> CommonResponse.success("Availability retrieved", slots));
    }

    @GetMapping("/students")
    public Mono<CommonResponse<List<TutorStudentResponse>>> getTutorStudents(
            @RequestAttribute("profileId") Long profileId) {
        return tutorProfileService.getTutorStudents(profileId)
                .map(res -> CommonResponse.success("Students retrieved", res));
    }

    @GetMapping("/tutorSubjects")
    public Mono<CommonResponse<List<TutorSubjectResponse>>> getTutorSubjects(
            @RequestAttribute("profileId") Long profileId) {
        return tutorProfileService.getTutorSubjects(profileId)
                .map(res -> CommonResponse.success("Subjects retrieved", res));
    }
}
