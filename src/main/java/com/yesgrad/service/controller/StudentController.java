package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.dto.StudentDashboardResponse;
import com.yesgrad.service.dto.StudentProfileResponse;
import com.yesgrad.service.service.AuthService;
import com.yesgrad.service.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentProfileService studentProfileService;
    private final AuthService authService;
    private static final String AUTH_COOKIE_NAME = "auth_token";

    @GetMapping("/profile")
    public Mono<ResponseEntity<CommonResponse<StudentProfile>>> getProfile(ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.getProfile(user.getId()))
                .map(profile -> ResponseEntity.ok(CommonResponse.success(profile)))
                .switchIfEmpty(Mono.just(ResponseEntity.status(404).body(
                        CommonResponse.error("NOT_FOUND", "Student profile not found"))));
    }

    @GetMapping("/profile/full")
    public Mono<ResponseEntity<CommonResponse<StudentProfileResponse>>> getFullProfile(ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.getFullProfile(user.getId()))
                .map(profile -> ResponseEntity.ok(CommonResponse.success(profile)));
    }

    @PutMapping("/profile")
    public Mono<ResponseEntity<CommonResponse<StudentProfile>>> updateProfile(
            @RequestBody com.yesgrad.service.domain.StudentProfileRequest request,
            ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.updateProfile(user.getId(), request))
                .map(profile -> ResponseEntity.ok(CommonResponse.success("Profile updated", profile)));
    }

    @PutMapping("/onboarding/basic-info")
    public Mono<ResponseEntity<CommonResponse<StudentProfile>>> saveBasicInfo(
            @RequestBody StudentProfileService.BasicInfoRequest request,
            ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.saveBasicInfo(user.getId(), request))
                .map(profile -> ResponseEntity.ok(CommonResponse.success("Basic info saved", profile)));
    }

    @PutMapping("/onboarding/subjects")
    public Mono<ResponseEntity<CommonResponse<List<StudentSubject>>>> saveSubjects(
            @RequestBody List<StudentProfileService.SubjectRequest> requests,
            ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.saveSubjects(user.getId(), requests))
                .map(subjects -> ResponseEntity.ok(CommonResponse.success("Subjects saved", subjects)));
    }

    @PutMapping("/onboarding/languages")
    public Mono<ResponseEntity<CommonResponse<List<StudentLanguage>>>> saveLanguages(
            @RequestBody List<StudentProfileService.LanguageRequest> requests,
            ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.saveLanguages(user.getId(), requests))
                .map(languages -> ResponseEntity.ok(CommonResponse.success("Languages saved", languages)));
    }

    @PutMapping("/onboarding/availability")
    public Mono<ResponseEntity<CommonResponse<List<StudentAvailability>>>> saveAvailability(
            @RequestBody List<StudentProfileService.AvailabilityRequest> requests,
            ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.saveAvailability(user.getId(), requests))
                .map(availability -> ResponseEntity.ok(CommonResponse.success("Availability saved", availability)));
    }

    @GetMapping("/dashboard")
    public Mono<ResponseEntity<CommonResponse<StudentDashboardResponse>>> getDashboard(ServerWebExchange exchange) {
        return extractTokenFromCookie(exchange)
                .flatMap(authService::getUserFromToken)
                .flatMap(user -> studentProfileService.getProfile(user.getId())
                        .flatMap(profile -> studentProfileService.getDashboard(profile.getId(), user.getId())))
                .map(res -> ResponseEntity.ok(CommonResponse.success("Dashboard Retrieved", res)));
    }

    private Mono<String> extractTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME);
        return cookie != null ? Mono.just(cookie.getValue()) : Mono.empty();
    }
}
