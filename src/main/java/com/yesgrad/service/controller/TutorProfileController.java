package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.service.FileStorageService;
import com.yesgrad.service.service.JwtAuthenticationToken;
import com.yesgrad.service.service.TutorProfileService;
import com.yesgrad.service.service.TutorSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api/tutor/profile")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;
    private final FileStorageService fileStorageService;
    private final TutorSettingsService tutorSettingsService;

    @GetMapping
    public Mono<CommonResponse<TutorProfileResponse>> getProfile(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return tutorProfileService.getProfileWithDetails(userId)
                .map(profile -> CommonResponse.success("Profile retrieved", profile))
                .defaultIfEmpty(CommonResponse.success("No profile found", null));
    }

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
                .map(profile -> CommonResponse.success("Profile updated successfully", profile));
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

    private TutorProfileRequest convertToRequest(TutorProfile profile) {
        TutorProfileRequest request = new TutorProfileRequest();
        request.setSchool(profile.getSchool());
        request.setDegree(profile.getDegree());
        request.setFieldOfStudy(profile.getFieldOfStudy());
        request.setGraduationYear(profile.getGraduationYear());
        request.setHourlyRate(profile.getHourlyRate());
        request.setCancellationPolicy(profile.getCancellationPolicy());
        request.setTravelPolicy(profile.getTravelPolicy());
        request.setSubjects(List.of());
        request.setLanguages(List.of());
        request.setAvailability(List.of());
        return request;
    }

}
