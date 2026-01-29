package com.yesgrad.service.dto;

import java.util.List;

public record TutorEducationRequest(
        List<EducationRequest> educations
) {}
