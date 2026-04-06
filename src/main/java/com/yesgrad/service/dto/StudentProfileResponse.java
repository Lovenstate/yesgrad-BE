package com.yesgrad.service.dto;

import com.yesgrad.service.domain.StudentProfile;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StudentProfileResponse {
    private Long id;
    private Long userId;
    private String bio;
    private String gradeLevel;
    private String school;
    private String learningGoals;
    private String learningStyle;
    private String timezone;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String lessonFormat;
    private String onboardingStatus;
    private Integer profileCompletion;
    private List<StudentSubjectResponse> subjects;
    private List<StudentLanguageResponse> languages;
    private List<StudentAvailabilityResponse> availability;

    @Data
    @Builder
    public static class StudentSubjectResponse {
        private Long id;
        private Long subjectId;
        private String subjectName;
        private String level;
    }

    @Data
    @Builder
    public static class StudentLanguageResponse {
        private Long id;
        private String language;
        private String proficiency;
    }

    @Data
    @Builder
    public static class StudentAvailabilityResponse {
        private Long id;
        private String dayOfWeek;
        private String startTime;
        private String endTime;
    }
}
