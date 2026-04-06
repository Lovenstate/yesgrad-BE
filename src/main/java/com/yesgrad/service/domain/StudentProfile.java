package com.yesgrad.service.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Table("student_profiles")
public class StudentProfile {
    
    @Id
    private Long id;
    
    @Column("user_id")
    private Long userId;

    @Column("bio")
    private String bio;
    
    @Column("grade_level")
    private String gradeLevel;
    
    @Column("school")
    private String school;
    
    @Column("learning_goals")
    private String learningGoals;
    
    @Column("learning_style")
    private String learningStyle;
    
    @Column("timezone")
    private String timezone;
    
    @Column("budget_min")
    private BigDecimal budgetMin;
    
    @Column("budget_max")
    private BigDecimal budgetMax;
    
    @Column("lesson_format")
    private LessonFormat lessonFormat;

    @Column("onboarding_status")
    private String onboardingStatus;

    @Column("profile_completion")
    private Integer profileCompletion;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    public enum LessonFormat {
        ONLINE, IN_PERSON, BOTH
    }
}
