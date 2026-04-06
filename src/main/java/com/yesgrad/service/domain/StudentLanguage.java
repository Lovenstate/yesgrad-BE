package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("student_languages")
public class StudentLanguage {
    @Id
    private Long id;
    private Long studentId;
    private String language;
    private String proficiency;
}
