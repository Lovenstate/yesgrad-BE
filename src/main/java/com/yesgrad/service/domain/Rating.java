package com.yesgrad.service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    private Long id;

    private Long lessonId;

    private Long tutorId;

    private Long studentId;

    private Integer rating;
    private String review;

    private LocalDateTime createdAt = LocalDateTime.now();
}
