package com.yesgrad.service.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private CommonError error;

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "Success", data, LocalDateTime.now(), null);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data, LocalDateTime.now(), null);
    }

    public static <T> CommonResponse<T> error(String code, String message) {
        CommonError error = new CommonError(code, message, null);
        return new CommonResponse<>(false, message, null, LocalDateTime.now(), error);
    }

    public static <T> CommonResponse<T> error(String code, String message, String details) {
        CommonError error = new CommonError(code, message, details);
        return new CommonResponse<>(false, message, null, LocalDateTime.now(), error);
    }

    public static <T> CommonResponse<T> error(String code, String message, T data) {
        CommonError error = new CommonError(code, message, null);
        return new CommonResponse<>(false, message, data, LocalDateTime.now(), error);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonError {
        private String code;
        private String message;
        private String details;
    }
}
