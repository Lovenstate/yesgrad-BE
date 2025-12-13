package com.yesgrad.service.service;

import com.yesgrad.service.domain.StudentProfile;
import com.yesgrad.service.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    
    private final StudentProfileRepository studentProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Mono<StudentProfile> completeOnboarding(Long userId, String gradeLevel, 
                                                   String learningGoals, BigDecimal budgetMin,
                                                   BigDecimal budgetMax, StudentProfile.LessonFormat lessonFormat,
                                                   List<String> subjects) {
        log.info("Completing onboarding for user ID: {} with subjects: {}", userId, subjects);
        
        return studentProfileRepository.findByUserId(userId)
            .switchIfEmpty(Mono.defer(() -> {
                StudentProfile profile = new StudentProfile();
                profile.setUserId(userId);
                profile.setCreatedAt(LocalDateTime.now());
                return Mono.just(profile);
            }))
            .flatMap(profile -> {
                profile.setGradeLevel(gradeLevel);
                profile.setLearningGoals(learningGoals);
                profile.setBudgetMin(budgetMin);
                profile.setBudgetMax(budgetMax);
                profile.setLessonFormat(lessonFormat);
                profile.setOnboardingCompleted(true);
                profile.setUpdatedAt(LocalDateTime.now());
                
                // Store subjects as JSON
                try {
                    profile.setSubjectsJson(objectMapper.writeValueAsString(subjects));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize subjects", e);
                }
                
                return studentProfileRepository.save(profile)
                    .doOnSuccess(saved -> log.info("Onboarding completed for user ID: {} with {} subjects", userId, subjects.size()));
            });
    }
    
    public Mono<StudentProfile> getProfile(Long userId) {
        log.debug("Fetching student profile for user ID: {}", userId);
        return studentProfileRepository.findByUserId(userId);
    }
}
