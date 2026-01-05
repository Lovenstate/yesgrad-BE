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
    private String bio;
    private Boolean instantBook;
    private String school;
    private String degree;
    private String fieldOfStudy;
    private Integer graduationYear;
    private Double hourlyRate;
    private String cancellationPolicy;
    private String travelPolicy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
