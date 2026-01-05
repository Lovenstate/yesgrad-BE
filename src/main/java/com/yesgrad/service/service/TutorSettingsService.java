package com.yesgrad.service.service;

import com.yesgrad.service.domain.*;
import com.yesgrad.service.repository.TutorProfileRepository;
import com.yesgrad.service.repository.TutorSettingsRepository;
import com.yesgrad.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TutorSettingsService {

    private final UserRepository userRepository;
    private final TutorProfileRepository profileRepository;
    private final TutorSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionalOperator transactionalOperator;

    public Mono<TutorCompleteSettingsResponse> getCompleteSettings(Long userId) {
        Mono<User> userMono = userRepository.findById(userId);
        Mono<TutorProfile> profileMono = profileRepository.findByUserId(userId);
        Mono<TutorSettings> settingsMono = settingsRepository.findByUserId(userId);

        return Mono.zip(userMono, profileMono.defaultIfEmpty(new TutorProfile()), settingsMono.defaultIfEmpty(new TutorSettings()))
                .map(tuple -> {
                    User user = tuple.getT1();
                    TutorProfile profile = tuple.getT2();
                    TutorSettings settings = tuple.getT3();

                    TutorCompleteSettingsResponse response = new TutorCompleteSettingsResponse();
                    response.setEmail(user.getEmail());
                    response.setPhone(user.getPhone());
                    response.setBio(profile.getBio());
                    response.setInstantBook(profile.getInstantBook());
                    response.setResponseTime(convertResponseTime(settings.getResponseTime()));
                    response.setEmailNotifications(settings.isEmailNotifications());
                    response.setSmsNotifications(settings.isSmsNotifications());
                    response.setLessonReminders(settings.isLessonReminders());
                    
                    return response;
                });
    }

    public Mono<TutorCompleteSettingsResponse> updateCompleteSettings(Long userId, TutorCompleteSettingsRequest request) {
        boolean passwordChanged = request.getCurrentPassword() != null && request.getNewPassword() != null;
        
        return transactionalOperator.transactional(
                Mono.zip(
                        updateUser(userId, request),
                        updateProfile(userId, request),
                        updateSettings(userId, request)
                ).then(getCompleteSettings(userId))
                        .map(response -> {
                            response.setPasswordChanged(passwordChanged);
                            return response;
                        })
        );
    }

    private Mono<User> updateUser(Long userId, TutorCompleteSettingsRequest request) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    boolean updated = false;
                    
                    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                        user.setEmail(request.getEmail());
                        updated = true;
                    }
                    
                    if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
                        user.setPhone(request.getPhone());
                        updated = true;
                    }
                    
                    if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
                        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                            return Mono.error(new IllegalArgumentException("Passwords do not match"));
                        }
                        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                            return Mono.error(new IllegalArgumentException("Current password is incorrect"));
                        }
                        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                        updated = true;
                    }
                    
                    if (updated) {
                        user.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(user);
                    }
                    
                    return Mono.just(user);
                });
    }

    private Mono<TutorProfile> updateProfile(Long userId, TutorCompleteSettingsRequest request) {
        return profileRepository.findByUserId(userId)
                .switchIfEmpty(createNewProfile(userId))
                .flatMap(profile -> {
                    boolean updated = false;
                    
                    if (request.getBio() != null) {
                        profile.setBio(request.getBio());
                        updated = true;
                    }
                    
                    if (request.getInstantBook() != null) {
                        profile.setInstantBook(request.getInstantBook());
                        updated = true;
                    }
                    
                    if (updated) {
                        profile.setUpdatedAt(LocalDateTime.now());
                        return profileRepository.save(profile);
                    }
                    
                    return Mono.just(profile);
                });
    }

    private Mono<TutorSettings> updateSettings(Long userId, TutorCompleteSettingsRequest request) {
        return settingsRepository.findByUserId(userId)
                .switchIfEmpty(createNewSettings(userId))
                .flatMap(settings -> {
                    boolean updated = false;
                    
                    if (request.getResponseTime() != null) {
                        settings.setResponseTime(parseResponseTime(request.getResponseTime()));
                        updated = true;
                    }
                    
                    if (request.getEmailNotifications() != null) {
                        settings.setEmailNotifications(request.getEmailNotifications());
                        updated = true;
                    }
                    
                    if (request.getSmsNotifications() != null) {
                        settings.setSmsNotifications(request.getSmsNotifications());
                        updated = true;
                    }
                    
                    if (request.getLessonReminders() != null) {
                        settings.setLessonReminders(request.getLessonReminders());
                        updated = true;
                    }
                    
                    if (updated) {
                        return settingsRepository.save(settings);
                    }
                    
                    return Mono.just(settings);
                });
    }

    private Mono<TutorProfile> createNewProfile(Long userId) {
        TutorProfile profile = new TutorProfile();
        profile.setUserId(userId);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    private Mono<TutorSettings> createNewSettings(Long userId) {
        TutorSettings settings = new TutorSettings();
        settings.setUserId(userId);
        settings.setEmailNotifications(true);
        settings.setSmsNotifications(false);
        settings.setLessonReminders(true);
        return settingsRepository.save(settings);
    }

    private Integer parseResponseTime(String responseTime) {
        return switch (responseTime.toLowerCase()) {
            case "1hour" -> 1;
            case "24hours" -> 24;
            case "48hours" -> 48;
            default -> 24;
        };
    }

    private String convertResponseTime(Integer hours) {
        if (hours == null) return "24hours";
        return switch (hours) {
            case 1 -> "1hour";
            case 48 -> "48hours";
            default -> "24hours";
        };
    }
}
