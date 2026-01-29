package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.dto.AvailabilityRequest;
import com.yesgrad.service.dto.TutorAvailabilityRequest;
import com.yesgrad.service.dto.TutorDashboardDTO;
import com.yesgrad.service.dto.TutorEducationRequest;
import com.yesgrad.service.service.JwtAuthenticationToken;
import com.yesgrad.service.service.TutorAvailabilityService;
import com.yesgrad.service.service.TutorDashboardService;
import com.yesgrad.service.service.TutorEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

    private final TutorDashboardService dashboardService;
    private final TutorEducationService tutorEducationService;
    private final TutorAvailabilityService tutorAvailabilityService;

    @GetMapping("/dashboard")
    public Mono<CommonResponse<TutorDashboardDTO>> getDashboard(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return dashboardService.getDashboardData(userId)
                .map(data -> CommonResponse.success("Dashboard data retrieved successfully", data));
    }

    // Tutor Subject Endpoints
    @PostMapping("/subjects")
    public Mono<CommonResponse<TutorSubject>> addTutorSubject(Authentication authentication, @RequestBody TutorSubjectRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return dashboardService.addTutorSubject(userId, request)
                .map(subject -> CommonResponse.success("Subject added successfully", subject));
    }

    @GetMapping("/subjects/search")
    public Flux<CommonResponse<TutorSubject>> getAllTutorSubjects(@RequestParam Long subjectId) {
        return dashboardService.findTutorSubjects(subjectId)
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



    //#### Tutor Profile Endpoints
//    POST   /api/tutors/apply           # Apply to become tutor
//    GET    /api/tutors/profile         # Get tutor profile
//    PUT    /api/tutors/profile         # Update tutor profile
//    POST   /api/tutors/documents       # Upload verification documents
//    GET    /api/tutors/status          # Get application status


//    #### Tutor Discovery
//```
//    GET    /api/tutors                 # Search/filter tutors
//    GET    /api/tutors/{id}            # Get tutor details
//    GET    /api/tutors/{id}/reviews    # Get tutor reviews
//    GET    /api/tutors/{id}/availability # Get tutor availability
//    POST   /api/tutors/{id}/favorite   # Add to favorites
//    DELETE /api/tutors/{id}/favorite   # Remove from favorites

//    #### Tutor Availability
//```
//    GET    /api/tutors/availability    # Get own availability
//    POST   /api/tutors/availability    # Set availability slots
//    PUT    /api/tutors/availability/{id} # Update availability slot
//    DELETE /api/tutors/availability/{id} # Delete availability slot
}
