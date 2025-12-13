package com.yesgrad.service.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Table("users")
public class User {
    
    @Id
    private Long id;
    
    @Column("email")
    private String email;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("phone")
    private String phone;
    
    @Column("zip_code")
    private String zipCode;
    
    @Column("role")
    private UserRole role;
    
    @Column("status")
    private UserStatus status;
    
    @Column("avatar_url")
    private String avatarUrl;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("last_login")
    private LocalDateTime lastLogin;
    
    public enum UserRole {
        STUDENT, TUTOR, ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }
}
