package com.yesgrad.service.service;

import com.yesgrad.service.domain.LoginResponse;
import com.yesgrad.service.domain.StudentProfile;
import com.yesgrad.service.domain.User;
import com.yesgrad.service.exceptions.AuthException;
import com.yesgrad.service.repository.StudentProfileRepository;
import com.yesgrad.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StudentProfileRepository studentProfileRepository;
    private final EmailService emailService;
    
    public Mono<User> register(String email, String password,
                               String firstName, String lastName,
                               String zipCode,
                               User.UserRole role) {
        log.info("Attempting to register user with email: {}", email);
        
        return userRepository.existsByEmail(email)
            .flatMap(exists -> {
                if (exists) {
                    log.warn("Registration failed: Email already exists - {}", email);
                    return Mono.error(new AuthException("EMAIL_EXISTS", "Email address is already registered"));
                }
                
                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setRole(role);
                user.setZipCode(zipCode);
                user.setStatus(User.UserStatus.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                
                return userRepository.save(user)
                    .flatMap(savedUser -> {
                        log.info("User registered successfully: {} (ID: {})", email, savedUser.getId());
                        
                        // Create student profile if role is STUDENT
                        if (savedUser.getRole() == User.UserRole.STUDENT) {
                            StudentProfile profile = new StudentProfile();
                            profile.setUserId(savedUser.getId());
                            profile.setOnboardingCompleted(false);
                            profile.setCreatedAt(LocalDateTime.now());
                            profile.setUpdatedAt(LocalDateTime.now());
                            
                            return studentProfileRepository.save(profile)
                                .thenReturn(savedUser)
                                .doOnSuccess(p -> log.info("Student profile created for user ID: {}", savedUser.getId()));
                        }
                        
                        return Mono.just(savedUser);
                    })
                    .doOnError(error -> log.error("Failed to save user: {}", email, error));
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
                
                if (user.getStatus() != User.UserStatus.ACTIVE) {
                    log.warn("Login failed: Account not active - {} (Status: {})", email, user.getStatus());
                    return Mono.error(new AuthException("ACCOUNT_INACTIVE", "Account is not active"));
                }
                
                user.setLastLogin(LocalDateTime.now());
                return userRepository.save(user)
                    .map(savedUser -> {
                        String token = jwtService.generateToken(savedUser);
                        log.info("User logged in successfully: {} (ID: {})", email, savedUser.getId());
                        return new LoginResponse(token, savedUser.getRole());
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

    public Mono<String> forgotPassword(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setResetToken(token);
                    user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

                    return userRepository.save(user)
                            .flatMap(savedUser ->  emailService.sendEmail(
                                    user.getEmail(),
                                    user.getFirstName(),
                                    token
                            )).thenReturn("Reset link to email");
                }).defaultIfEmpty("Reset link to email");
    }

    public Mono<String> resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid or expired reset token")))
                .flatMap(user -> {
                    if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        return Mono.error(new IllegalArgumentException("Reset token has expired"));
                    }
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user).thenReturn("Password updated successfully");
                });
    }

    public Mono<Boolean> verifyResetToken(String token) {
        return userRepository.findByResetToken(token)
                .map(user -> user.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .defaultIfEmpty(false);
    }
}