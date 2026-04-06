package com.yesgrad.service.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "messages")
@Data
public class Message {
    @Id
    private Long id;

    private Long senderId;
    private Long receiverId;
    private String content;
    private Long replyToMessageId;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private Boolean isRead;

    public Message() {}

    public Message(Long senderId, Long receiverId, String content) {
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
        this.content = content;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    public Message(Long senderId, Long receiverId, String content, Long replyToMessageId) {
        this(senderId, receiverId, content);
        this.replyToMessageId = replyToMessageId;
    }
}
