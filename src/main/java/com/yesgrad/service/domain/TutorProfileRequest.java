package com.yesgrad.service.domain;

import lombok.Data;

import java.util.List;

@Data
public class TutorProfileRequest {
    private String school;
    private String degree;
    private String fieldOfStudy;
    private Integer graduationYear;
    private Double hourlyRate;
    private String cancellationPolicy;
    private String headline;
    private String bio;
    private String travelPolicy;
}
