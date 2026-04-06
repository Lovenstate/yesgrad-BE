package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("tutor_education")
public class TutorEducation {

    // must be a list of education
    @Id
    private Long id;
    private Long tutorId;
    private String school;
    private String degree;
    private String fieldOfStudy;
    private Integer graduationYear;
}