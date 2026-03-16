package com.example.contactmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "AuthRequest", description = "Request for authentication")
public record AuthRequest(

        @Schema(description = "Username for authentication", example = "john")
        @NotBlank
        String username,

        @Schema(description = "Password for authentication", example = "password123")
        @NotBlank
        String password
) {}
