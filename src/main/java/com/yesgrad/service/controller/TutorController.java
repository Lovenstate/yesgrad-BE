package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.domain.TutorSubject;
import com.yesgrad.service.domain.TutorSubjectRequest;
import com.yesgrad.service.dto.TutorDashboardDTO;
import com.yesgrad.service.service.JwtAuthenticationToken;
import com.yesgrad.service.service.TutorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

    private final TutorDashboardService dashboardService;

    @GetMapping("/dashboard")
    public Mono<CommonResponse<TutorDashboardDTO>> getDashboard(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return dashboardService.getDashboardData(userId)
                .map(data -> CommonResponse.success("Dashboard data retrieved successfully", data));
    }

    @PostMapping("/subjects")
    public Mono<CommonResponse<TutorSubject>> addTutorSubject(Authentication authentication, @RequestBody TutorSubjectRequest request) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return dashboardService.addTutorSubject(userId, request)
                .map(subject -> CommonResponse.success("Subject added successfully", subject));
    }

    @GetMapping("/subjects/search")
    public Flux<CommonResponse<TutorSubject>> getAllTutorSubjects(@RequestParam Long subjectId, @RequestParam Long levelId) {
        return dashboardService.findTutorSubjects(subjectId, levelId)
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
