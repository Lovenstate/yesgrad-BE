package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.dto.OnboardingStatusResponse;
import com.yesgrad.service.service.TutorCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/tutor")
@RequiredArgsConstructor
public class TutorOnboardingController {

    private final TutorCompletionService tutorCompletionService;

    @GetMapping("/{tutorId}/onboarding")
    public Mono<CommonResponse<OnboardingStatusResponse>> getOnboardingStatus(@PathVariable Long tutorId) {
        return tutorCompletionService.calculate(tutorId)
                .map(CommonResponse::success);
    }
}
