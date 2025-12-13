package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("tutor_languages")
public class TutorLanguage {

    @Id
    private Long id;
    private Long tutorId;
    private String language;
    private String proficiency;
}
