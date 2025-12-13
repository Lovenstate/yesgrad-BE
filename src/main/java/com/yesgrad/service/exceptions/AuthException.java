package com.yesgrad.service.exceptions;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final String errorCode;

    public AuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
