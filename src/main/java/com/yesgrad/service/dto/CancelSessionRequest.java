package com.yesgrad.service.dto;

public record CancelSessionRequest(
        Long userId,
        String reason
) {
}
