package com.example.contactmanager.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "ApiError", description = "Standard API error response")
public record ApiError(

        @Schema(example = "404")
        int status,

        @Schema(example = "Not Found")
        String error,

        @Schema(example = "Contact not found with id: 10")
        String message,

        @Schema(example = "/contacts/10")
        String path,

        @Schema(example = "2026-03-15T20:15:30")
        LocalDateTime timestamp
) {
    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now());
    }
}