package com.yesgrad.service.domain;

public record LoginResponse(String token, User.UserRole role) {
}
