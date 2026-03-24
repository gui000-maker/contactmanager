package com.example.contactmanager.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for refresh token")
public record RefreshRequest (

        @NotBlank
        @Schema(description = "Opaque refresh token", example = "a3f1c2d4-e5b6-7890-abcd-ef1234567890")
        String refreshToken
) {}
