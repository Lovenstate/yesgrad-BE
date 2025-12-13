package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.domain.TutorProfile;
import com.yesgrad.service.domain.TutorProfileRequest;
import com.yesgrad.service.domain.TutorProfileResponse;
import com.yesgrad.service.service.FileStorageService;
import com.yesgrad.service.service.TutorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tutor/profile")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public Mono<CommonResponse<TutorProfileResponse>> getProfile(@AuthenticationPrincipal Long userId) {
        return tutorProfileService.getProfileWithDetails(userId)
                .map(profile -> CommonResponse.success("Profile retrieved", profile))
                .defaultIfEmpty(CommonResponse.success("No profile found", null));
    }

    @PutMapping
    public Mono<CommonResponse<TutorProfile>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody TutorProfileRequest request) {
        return tutorProfileService.updateProfile(userId, request)
                .map(profile -> CommonResponse.success("Profile updated successfully", profile));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<CommonResponse<String>> uploadPhoto(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") FilePart filePart) {
        return fileStorageService.saveProfilePhoto(filePart)
                .flatMap(photoUrl -> tutorProfileService.getProfile(userId)
                        .flatMap(profile -> {
                            profile.setProfilePhotoUrl(photoUrl);
                            return tutorProfileService.updateProfile(userId, convertToRequest(profile));
                        })
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
        return request;
    }

}
