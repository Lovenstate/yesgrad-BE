package com.yesgrad.service.dto.message;

public record SendMessageRequest(
        Long senderId,
        Long receiverId,
        String content,
        Long replyToMessageId
) {
}
