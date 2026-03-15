package com.example.contactmanager.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ApiError(

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Error description", example = "Not Found")
        String error,

        @Schema(description = "Error message", example = "Resource not found")
        String message,

        @Schema(description = "Request path", example = "/api/resource")
        String path,

        @Schema(description = "Timestamp of the error", example = "2023-07-25T12:00:00")
        LocalDateTime timestamp
) {
    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now());
    }
}
