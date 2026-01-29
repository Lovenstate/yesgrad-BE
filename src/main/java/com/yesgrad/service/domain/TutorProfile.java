package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
@Data
@Table("tutor_profiles")
public class TutorProfile {
    @Id
    private Long id;
    private Long userId;
    private String profilePhotoUrl;
    private String headline;
    private String bio;
    private Boolean instantBook;
    private String onboardingStatus;  // STARTED | COMPLETE
    private Integer profileCompletion; // 0–100
    private String cancellationPolicy;
    private String travelPolicy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
