package com.yesgrad.service.dto;

public record EducationRequest(
        String school,
        String degree,
        String fieldOfStudy,
        Integer graduationYear
) {}