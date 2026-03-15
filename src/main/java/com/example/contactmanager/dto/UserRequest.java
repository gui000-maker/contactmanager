package com.example.contactmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record UserRequest (

        @Schema(description = "Username of the user", example = "john_doe")
        @NotBlank(message = "Username cannot be empty")
        @Size(max = 50, message = "Username must be at most 50 characters")
        String username,

        @Schema(description = "Password of the user", example = "password123")
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
