package com.yesgrad.service.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TutorProfileResponse {
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
    private List<String> subjects;
    private List<LanguageDto> languages;
    private List<AvailabilityDto> availability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
