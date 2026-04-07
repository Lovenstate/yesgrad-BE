package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.enums.UserRole;
import com.yesgrad.service.service.AuthService;
import com.yesgrad.service.service.TutorProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final TutorProfileService tutorProfileService;
    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final int COOKIE_MAX_AGE = 86400; // 24 hours

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/register")
    public Mono<ResponseEntity<CommonResponse<User>>> register(
            @Valid @RequestBody RegisterRequest request,
            ServerHttpResponse response) {
        
        return authService.register(request)
            .flatMap(user -> {
                // Auto-login only for non-tutor users
                if (user.getRole() != UserRole.TUTOR) {
                     return authService.login(request.email(), request.password())
                            .map(loginResponse -> {
                                setAuthCookie(response, loginResponse.token());
                                return user;
                            });
                }
                // Tutors need to verify email first
                return Mono.just(user);
            })
            .map(user -> ResponseEntity.ok(CommonResponse.success("Registration successful", user)))
            .doOnError(error -> log.error("Registration failed for email: {}", request.email(), error));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<CommonResponse<LoginResponseDTO>>> login(
            @Valid @RequestBody LoginRequest request,
            ServerHttpResponse response) {
        log.info("Login request received for email: {}", request.email);
        
        return authService.login(request.email, request.password)
            .map(loginResponse -> {
                setAuthCookie(response, loginResponse.token());
                log.info("Login successful for email: {}", request.email);
                return ResponseEntity.ok(CommonResponse.success(new LoginResponseDTO(
                        loginResponse.role(),
                        loginResponse.firstLogin(),
                        loginResponse.onboardingStatus(),
                        loginResponse.profileCompletion(),
                        loginResponse.emailVerified()
                )));
            })
            .doOnError(error -> log.error("Login failed for email: {}", request.email));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<CommonResponse<String>>> logout(ServerHttpResponse response) {
        log.info("Logout request received");
        clearAuthCookie(response);
        log.debug("Auth cookie cleared");
        return Mono.just(ResponseEntity.ok(CommonResponse.success("Logout successful")));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<CommonResponse<User>>> getCurrentUser(ServerWebExchange exchange) {
        log.debug("Get current user request received");
        
        return extractTokenFromCookie(exchange)
            .flatMap(authService::getUserFromToken)
            .map(user -> {
                log.debug("Current user retrieved: {} (ID: {})", user.getEmail(), user.getId());
                return ResponseEntity.ok(CommonResponse.success(user));
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("Unauthorized access attempt - no valid token found");
                return Mono.just(
                    ResponseEntity.status(401).body(
                        CommonResponse.error("UNAUTHORIZED", "Not authenticated")
                    )
                );
            }));
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<CommonResponse<String>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.email());

        return authService.forgotPassword(request.email(), request.ipAddress())
                .map(message -> ResponseEntity.ok(CommonResponse.success(message)))
                .doOnError(error -> log.error("Forgot password failed", error));
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<CommonResponse<String>>> restPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return authService.resetPassword(request.token(), request.newPassword())
                .map(message -> ResponseEntity.ok(CommonResponse.success(message)))
                .doOnError(error -> log.error("Reset password failed", error));
    }

    @GetMapping("/verify-reset-token/{token}")
    public Mono<ResponseEntity<CommonResponse<Boolean>>> verifyResetToken(@PathVariable String token) {
        return authService.verifyResetToken(token)
                .map(valid -> ResponseEntity.ok(CommonResponse.success("Token verified", valid)));
    }

    @PostMapping("/verify-email")
    public Mono<ResponseEntity<CommonResponse<Boolean>>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification request received");
        return authService.verifyEmail(request.token())
                .map(message -> ResponseEntity.ok(CommonResponse.success(message)))
                .doOnError(error -> log.error("Email verification failed", error));
    }

    @PostMapping("/resend-verification")
    public Mono<ResponseEntity<CommonResponse<String>>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request for email: {}", request.email());
        return authService.resendVerificationEmail(request.email())
                .map(message -> ResponseEntity.ok(CommonResponse.success(message)))
                .doOnError(error -> log.error("Resend verification failed", error));
    }

    private void setAuthCookie(ServerHttpResponse response, String token) {
        boolean isSecure = frontendUrl.startsWith("https");
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(COOKIE_MAX_AGE)
            .sameSite(isSecure ? "None" : "Lax")
            .build();
        response.addCookie(cookie);
    }

    private void clearAuthCookie(ServerHttpResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .build();
        response.addCookie(cookie);
    }

    private Mono<String> extractTokenFromCookie(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME);
        return cookie != null ? Mono.just(cookie.getValue()) : Mono.empty();
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String zipCode,
            @NotBlank String role
    ) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank String ipAddress
    ) {}

    public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword
    ) {}

    public record VerifyEmailRequest(
        @NotBlank String token
    ) {}

    public record ResendVerificationRequest(
        @NotBlank @Email String email
    ) {}

    public record LoginResponseDTO(UserRole role,
                                   boolean isFirstLogin,
                                   String onboardingStatus,
                                   Integer profileCompletion,
                                   Boolean emailVerified) {}
}
