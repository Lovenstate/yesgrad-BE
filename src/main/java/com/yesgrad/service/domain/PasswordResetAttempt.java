package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("password_reset_attempts")
public class PasswordResetAttempt {
    @Id
    private Long id;
    private String email;
    private String ipAddress;
    private LocalDateTime attemptedAt;
}
