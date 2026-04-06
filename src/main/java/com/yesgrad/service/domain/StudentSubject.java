package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("student_subjects")
public class StudentSubject {
    @Id
    private Long id;
    private Long studentId;
    private Long subjectId;
    private String level;
}
