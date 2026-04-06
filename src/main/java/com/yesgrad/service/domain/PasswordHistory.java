package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("password_history")
public class PasswordHistory {
    @Id
    private Long id;
    private Long userId;
    private String passwordHash;
    private LocalDateTime createdAt;
}
