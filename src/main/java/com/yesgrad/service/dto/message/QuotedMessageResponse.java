package com.yesgrad.service.dto.message;

public record QuotedMessageResponse(
        Long id,
        Long senderId,
        String contentPreview // first 120 chars
) {
}
