package com.yesgrad.service.controller;

import com.yesgrad.service.domain.CommonResponse;
import com.yesgrad.service.domain.StudentProfile;
import com.yesgrad.service.service.AuthService;
import com.yesgrad.service.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;
    private static final String AUTH_COOKIE_NAME = "auth_token";

    @PostMapping("/onboarding")
    public Mono<ResponseEntity<CommonResponse<StudentProfile>>> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request,
            ServerWebExchange exchange) {
        log.info("Onboarding request received: subjects={}, gradeLevel={}, budget={}, format={}",
                request.subjects, request.gradeLevel, request.budget, request.lessonFormat);
        
        // Parse budget range (e.g., "20-30" -> min=20, max=30)
        String[] budgetParts = request.budget.replace("+", "").split("-");
        BigDecimal budgetMin = new BigDecimal(budgetParts[0]);
        BigDecimal budgetMax = budgetParts.length > 1 ? new BigDecimal(budgetParts[1]) : new BigDecimal("999.99");
        
        // Convert lesson format
        String format = request.lessonFormat.toUpperCase().replace("-", "_");
        
        return extractTokenFromCookie(exchange)
            .flatMap(authService::getUserFromToken)
            .flatMap(user -> studentService.completeOnboarding(
                user.getId(),
                request.gradeLevel,
                request.learningGoals,
                budgetMin,
                budgetMax,
                StudentProfile.LessonFormat.valueOf(format),
                request.subjects
            ))
            .map(profile -> {
                log.info("Onboarding completed successfully for user");
                return ResponseEntity.ok(CommonResponse.success("Onboarding completed", profile));
            })
            .switchIfEmpty(Mono.just(
                ResponseEntity.status(401).body(
                    CommonResponse.error("UNAUTHORIZED", "Not authenticated")
                )
            ));
    }

    @GetMapping("/profile")
    public Mono<ResponseEntity<CommonResponse<StudentProfile>>> getProfile(ServerWebExchange exchange) {
        log.debug("Get student profile request");
        
        return extractTokenFromCookie(exchange)
            .flatMap(authService::getUserFromToken)
            .flatMap(user -> studentService.getProfile(user.getId()))
            .map(profile -> ResponseEntity.ok(CommonResponse.success(profile)))
            .switchIfEmpty(Mono.just(
                ResponseEntity.status(404).body(
                    CommonResponse.error("NOT_FOUND", "Student profile not found")
                )
            ));
    }

    @GetMapping("/dashboard")
    public Mono<ResponseEntity<CommonResponse<DashboardData>>> getDashboard(ServerWebExchange exchange) {
        log.debug("Get student dashboard request");
        
        return extractTokenFromCookie(exchange)
            .flatMap(authService::getUserFromToken)
            .flatMap(user -> {
                // TODO: Implement dashboard data aggregation
                DashboardData data = new DashboardData(0, 0, 0, List.of(), List.of());
                return Mono.just(ResponseEntity.ok(CommonResponse.success(data)));
            })
            .switchIfEmpty(Mono.just(
                ResponseEntity.status(401).body(
                    CommonResponse.error("UNAUTHORIZED", "Not authenticated")
                )
            ));
    }

    private Mono<String> extractTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME);
        return cookie != null ? Mono.just(cookie.getValue()) : Mono.empty();
    }

    public record OnboardingRequest(
        List<String> subjects,
        @NotBlank String gradeLevel,
        @NotBlank String learningGoals,
        @NotBlank String budget,
        @NotBlank String lessonFormat
    ) {}

    public record DashboardData(
        int upcomingSessions,
        int totalHours,
        int activeTutors,
        List<Object> recentSessions,
        List<Object> messages
    ) {}
}
