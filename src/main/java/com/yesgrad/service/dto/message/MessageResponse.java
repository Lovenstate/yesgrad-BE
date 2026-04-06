package com.yesgrad.service.dto.message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        Long replyToMessageId,
        QuotedMessageResponse quotedMessage,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        Boolean isRead
) {
}