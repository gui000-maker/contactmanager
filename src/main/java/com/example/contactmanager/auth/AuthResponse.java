package com.example.contactmanager.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication response containing access and refresh tokens")
public record AuthResponse(

        @Schema(description = "Username for authentication", example = "john")
        String username,

        @NotBlank
        @Schema(
                description = "JWT access token used for authenticated requests",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @NotBlank
        @Schema(
                description = "Opaque token used to obtain a new access token. Store securely.",
                example = "a3f1c2d4-e5b6-7890-abcd-ef1234567890"  // ← UUID format
        )
        String refreshToken
) {}