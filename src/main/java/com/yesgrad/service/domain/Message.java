package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "messages")
@Data
public class Message {
    private Long id;

    private Long senderId;
    private Long receiverId;
    private String content;

    private LocalDateTime sentAt = LocalDateTime.now();
    private LocalDateTime readAt;
    private Boolean isRead = false;
}
