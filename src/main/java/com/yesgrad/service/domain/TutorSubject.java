package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("tutor_subjects")
public class TutorSubject {
    @Id
    private Long id;
    private Long tutorId;
    private Long subjectId;
    private String proficiencyLevel;
}
