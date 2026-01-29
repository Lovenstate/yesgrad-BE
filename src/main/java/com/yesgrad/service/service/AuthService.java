package com.yesgrad.service.service;

import com.yesgrad.service.config.PropertiesConfig;
import com.yesgrad.service.controller.AuthController;
import com.yesgrad.service.domain.*;
import com.yesgrad.service.enums.UserRole;
import com.yesgrad.service.enums.UserStatus;
import com.yesgrad.service.exceptions.AuthException;
import com.yesgrad.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final TutorProfileRepository tutorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StudentProfileRepository studentProfileRepository;
    private final EmailService emailService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetAttemptRepository resetAttemptRepository;
    private final PropertiesConfig config;
    
    public Mono<User> register(AuthController.RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.email());
        
        return userRepository.existsByEmail(request.email())
            .flatMap(exists -> {
                if (exists) {
                    log.warn("Registration failed: Email already exists - {}", request.email());
                    return Mono.error(new AuthException("EMAIL_EXISTS", "Email address is already registered"));
                }
                
                User user = new User();
                user.setEmail(request.email());
                user.setPasswordHash(passwordEncoder.encode(request.password()));
                user.setFirstName(request.firstName());
                user.setLastName(request.lastName());
                user.setRole(UserRole.valueOf(request.role()));
                user.setZipCode(request.zipCode());
                user.setStatus(UserStatus.ACTIVE);
                user.setEmailVerified(false);
                user.setFirstLogin(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                
                return userRepository.save(user)
                    .flatMap(savedUser -> {
                        log.info("User registered successfully: {} (ID: {})", request.email(), savedUser.getId());
                        
                        // Send verification email
                        String verificationToken = jwtService.generateResetToken(savedUser.getEmail());
                        savedUser.setVerificationToken(verificationToken);
                        savedUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                        
                        return userRepository.save(savedUser)
                                .flatMap(userWithToken -> {
                                    String verificationLink = config.getFrontend().getUrl() + "/auth/verify-email?token=" + verificationToken;
                                    
                                    // Send email and wait for completion
                                    return emailService.sendEmail(
                                            "email-verification",
                                            userWithToken.getEmail(),
                                            userWithToken.getFirstName(),
                                            Map.of("verificationLink", verificationLink)
                                    ).onErrorResume(error -> {
                                        log.error("Failed to send verification email to: {}", userWithToken.getEmail(), error);
                                        // Continue even if email fails
                                        return Mono.empty();
                                    }).then(Mono.defer(() -> {
                                        if (userWithToken.getRole() == UserRole.STUDENT) {
                                        StudentProfile profile = new StudentProfile();
                                        profile.setUserId(userWithToken.getId());
                                        profile.setOnboardingCompleted(false);
                                        profile.setCreatedAt(LocalDateTime.now());
                                        profile.setUpdatedAt(LocalDateTime.now());
                                        return studentProfileRepository.save(profile).thenReturn(userWithToken);
                                    }

                                        if (userWithToken.getRole() == UserRole.TUTOR) {
                                            TutorProfile profile = new TutorProfile();
                                            profile.setUserId(userWithToken.getId());
                                            profile.setOnboardingStatus("STARTED");
                                            profile.setCreatedAt(LocalDateTime.now());
                                            profile.setUpdatedAt(LocalDateTime.now());
                                            return tutorProfileRepository.save(profile).thenReturn(userWithToken);
                                        }

                                        return Mono.just(userWithToken);
                                    }));
                                });
                    })
                    .doOnError(error -> log.error("Failed to save user: {}", request.email(), error));
            });
    }
    
    public Mono<LoginResponse> login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("Login failed: User not found - {}", email);
                return Mono.error(new AuthException("INVALID_CREDENTIALS", "Invalid email or password"));
            }))
            .flatMap(user -> {
                if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                    log.warn("Login failed: Invalid password for user - {}", email);
                    return Mono.error(new AuthException("INVALID_CREDENTIALS", "Invalid email or password"));
                }
                
                if (user.getStatus() != UserStatus.ACTIVE) {
                    log.warn("Login failed: Account not active - {} (Status: {})", email, user.getStatus());
                    return Mono.error(new AuthException("ACCOUNT_INACTIVE", "Account is not active"));
                }
                
                // Check email verification for tutors
                if (user.getRole() == UserRole.TUTOR && !Boolean.TRUE.equals(user.getEmailVerified())) {
                    log.warn("Login failed: Email not verified - {}", email);
                    return Mono.error(new AuthException("EMAIL_NOT_VERIFIED", "Please verify your email before logging in"));
                }
                
                user.setLastLogin(LocalDateTime.now());
                boolean isFirstLogin = Boolean.TRUE.equals(user.getFirstLogin());
                user.setFirstLogin(false);
                return userRepository.save(user)
                    .map(savedUser -> {
                        String token = jwtService.generateToken(savedUser);
                        log.info("User logged in successfully: {} (ID: {})", email, savedUser.getId());
                        return new LoginResponse(token, savedUser.getRole(), isFirstLogin);
                    });
            })
            .doOnError(error -> {
                if (!(error instanceof AuthException)) {
                    log.error("Unexpected error during login for user: {}", email, error);
                }
            });
    }
    
    public Mono<User> getUserFromToken(String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid or expired token");
                return Mono.empty();
            }
            
            String email = jwtService.extractEmail(token);
            log.debug("Fetching user from token: {}", email);
            
            return userRepository.findByEmail(email)
                .doOnSuccess(user -> {
                    if (user != null) {
                        log.debug("User found from token: {} (ID: {})", email, user.getId());
                    } else {
                        log.warn("User not found for email from token: {}", email);
                    }
                });
        } catch (Exception e) {
            log.error("Error extracting user from token", e);
            return Mono.empty();
        }
    }

    public Mono<String> forgotPassword(String email, String ipAddress) {
        // Rate limiting check
        LocalDateTime windowStart = LocalDateTime.now().minusHours(config.getSecurity().getResetRateWindowHours());
        
        return resetAttemptRepository.countRecentAttempts(email, windowStart)
                .flatMap(count -> {
                    if (count >= config.getSecurity().getResetRateLimit()) {
                        log.warn("Rate limit exceeded for password reset: {}", email);
                        return Mono.just("Reset link sent to email"); // Same response for security
                    }
                    
                    // Log attempt
                    PasswordResetAttempt attempt = new PasswordResetAttempt();
                    attempt.setEmail(email);
                    attempt.setIpAddress(ipAddress);
                    attempt.setAttemptedAt(LocalDateTime.now());
                    
                    return resetAttemptRepository.save(attempt)
                            .then(userRepository.findByEmail(email))
                            .flatMap(user -> {
                                // Generate signed token using JWT
                                String token = jwtService.generateResetToken(user.getEmail());
                                user.setResetToken(token);
                                user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

                                return userRepository.save(user)
                                        .flatMap(savedUser -> {
                                            String resetLink = config.getFrontend().getUrl() + "/auth/reset-password?token=" + token;
                                            // Async email with error handling
                                            return emailService.sendEmail(
                                                    "password-reset",
                                                    user.getEmail(),
                                                    user.getFirstName(),
                                                    Map.of("resetLink", resetLink)
                                            ).onErrorResume(error -> {
                                                log.error("Failed to send password reset email to: {}", user.getEmail(), error);
                                                return Mono.empty();
                                            }).thenReturn("Reset link sent to email");
                                        });
                            })
                            .defaultIfEmpty("Reset link sent to email");
                });
    }

    public Mono<String> resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .switchIfEmpty(Mono.error(new AuthException("INVALID_TOKEN", "Invalid or expired reset token")))
                .flatMap(user -> {
                    if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        return Mono.error(new AuthException("TOKEN_EXPIRED", "Reset token has expired"));
                    }
                    
                    // Check password history
                    return passwordHistoryRepository.findRecentByUserId(user.getId(), config.getSecurity().getPasswordHistoryLimit())
                            .collectList()
                            .flatMap(history -> {
                                // Check if new password matches any recent password
                                boolean passwordReused = history.stream()
                                        .anyMatch(h -> passwordEncoder.matches(newPassword, h.getPasswordHash()));
                                
                                if (passwordReused) {
                                    return Mono.error(new AuthException("PASSWORD_REUSED", 
                                            "Cannot reuse any of your last " + config.getSecurity().getPasswordHistoryLimit() + " passwords"));
                                }
                                
                                // Save current password to history
                                PasswordHistory passwordHistory = new PasswordHistory();
                                passwordHistory.setUserId(user.getId());
                                passwordHistory.setPasswordHash(user.getPasswordHash());
                                passwordHistory.setCreatedAt(LocalDateTime.now());
                                
                                return passwordHistoryRepository.save(passwordHistory)
                                        .then(Mono.defer(() -> {
                                            // Update password and clear reset token
                                            user.setPasswordHash(passwordEncoder.encode(newPassword));
                                            user.setResetToken(null);
                                            user.setResetTokenExpiry(null);
                                            user.setUpdatedAt(LocalDateTime.now());

                                            return userRepository.save(user)
                                                    .flatMap(savedUser -> {
                                                        // Send confirmation email async with error handling
                                                        emailService.sendEmail(
                                                                "password-reset-success",
                                                                user.getEmail(),
                                                                user.getFirstName(),
                                                                java.util.Map.of()
                                                        ).doOnError(error -> log.error("Failed to send confirmation email to: {}", user.getEmail(), error))
                                                         .subscribe();
                                                        
                                                        return Mono.just("Password updated successfully");
                                                    });
                                        }));
                            });
                });
    }

    public Mono<Boolean> verifyResetToken(String token) {
        return userRepository.findByResetToken(token)
                .map(user -> user.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> verifyEmail(String token) {
        return userRepository.findByVerificationToken(token)
                .switchIfEmpty(Mono.error(new AuthException("INVALID_TOKEN", "Invalid verification token")))
                .flatMap(user -> {
                    if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                        return Mono.error(new AuthException("TOKEN_EXPIRED", "Verification token has expired"));
                    }
                    
                    user.setEmailVerified(true);
                    user.setVerificationToken(null);
                    user.setVerificationTokenExpiry(null);
                    user.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(user)
                            .thenReturn(Boolean.TRUE);
                });
    }

    public Mono<String> resendVerificationEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new AuthException("USER_NOT_FOUND", "User not found")))
                .flatMap(user -> {
                    if (Boolean.TRUE.equals(user.getEmailVerified())) {
                        return Mono.error(new AuthException("ALREADY_VERIFIED", "Email already verified"));
                    }
                    
                    String verificationToken = jwtService.generateResetToken(user.getEmail());
                    user.setVerificationToken(verificationToken);
                    user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                    
                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                String verificationLink = config.getFrontend().getUrl() + "/auth/verify-email?token=" + verificationToken;
                                return emailService.sendEmail(
                                        "email-verification",
                                        user.getEmail(),
                                        user.getFirstName(),
                                        Map.of("verificationLink", verificationLink)
                                ).onErrorResume(error -> {
                                    log.error("Failed to send verification email to: {}", user.getEmail(), error);
                                    return Mono.empty();
                                }).thenReturn("Verification email sent");
                            });
                });
    }
}