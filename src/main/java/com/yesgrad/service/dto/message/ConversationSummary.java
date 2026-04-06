package com.yesgrad.service.dto.message;

import java.time.LocalDateTime;

public record ConversationSummary(
        Long otherUserId,
        String lastMessageContent,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}
