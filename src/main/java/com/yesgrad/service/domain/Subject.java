package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("subjects")
public class Subject {
    
    @Id
    private Long id;
    
    @Column("name")
    private String name;
    
    @Column("description")
    private String description;
    
    @Column("category")
    private String category;
    
    @Column("is_active")
    private Boolean isActive;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
