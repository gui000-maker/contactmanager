package com.example.contactmanager.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RegisterRequest", description = "Request for register")
public record RegisterRequest (

        @Schema(description = "Username for authentication", example = "john")
        @NotBlank
        String username,

        @Schema(description = "Password for authentication", example = "password123")
        @NotBlank
        String password

){ }
