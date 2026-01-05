package com.yesgrad.service.controller;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final int COOKIE_MAX_AGE = 3600; // 1 hour

    @PostMapping("/register")
    public Mono<ResponseEntity<CommonResponse<User>>> register(
            @Valid @RequestBody RegisterRequest request,
            ServerHttpResponse response) {
        log.info("Registration request received for email: {}", request.email);
        
        return authService.register(
                request.email,
                request.password,
                request.firstName,
                request.lastName,
                request.zipCode,
                User.UserRole.valueOf(request.role.toUpperCase())
            )
            .flatMap(user -> authService.login(request.email, request.password)
                .map(loginResponse -> {
                    setAuthCookie(response, loginResponse.token());
                    log.info("User registered and logged in successfully: {}", request.email);
                    return ResponseEntity.ok(CommonResponse.success("Registration successful", user));
                })
            )
            .doOnError(error -> log.error("Registration failed for email: {}", request.email, error));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<CommonResponse<String>>> login(
            @Valid @RequestBody LoginRequest request,
            ServerHttpResponse response) {
        log.info("Login request received for email: {}", request.email);
        
        return authService.login(request.email, request.password)
            .map(loginResponse -> {
                setAuthCookie(response, loginResponse.token());
                log.info("Login successful for email: {}", request.email);
                return ResponseEntity.ok(CommonResponse.success(loginResponse.role().toString()));
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

        return authService.forgotPassword(request.email())
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

    private void setAuthCookie(ServerHttpResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(false) // Set to true in production with HTTPS
            .path("/")
            .maxAge(COOKIE_MAX_AGE)
            .sameSite("Lax") // Use "Strict" for more security, "Lax" for better compatibility
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
        @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6) String newPassword
    ) {}
}
