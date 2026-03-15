package com.example.contactmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserResponse(
        @Schema(description = "ID of the user", example = "1")
        Long id,

        @Schema(description = "Username of the user", example = "john_doe")
        String username,

        @Schema(description = "Role of the user", example = "ADMIN")
        String role,

        @Schema(description = "Creation date of the user", example = "2023-07-25T12:00:00")
        LocalDateTime createdAt
) {}
