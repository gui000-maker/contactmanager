package com.example.contactmanager.exception;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now());
    }
}
