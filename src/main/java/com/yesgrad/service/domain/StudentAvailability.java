package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalTime;

@Data
@Table("student_availability")
public class StudentAvailability {
    @Id
    private Long id;
    private Long studentId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
