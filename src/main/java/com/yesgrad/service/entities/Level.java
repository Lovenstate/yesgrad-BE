package com.yesgrad.service.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("levels")
public class Level {
    @Id
    private Long id;
    private String name;
}
