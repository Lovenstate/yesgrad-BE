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

    @Column("slug")
    private String slug;

    @Column("parent_id")
    private Long parentId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
