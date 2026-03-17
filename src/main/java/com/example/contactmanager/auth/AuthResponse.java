package com.example.contactmanager.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Response for authentication")
public record AuthResponse(

        @Schema(description = "Username for authentication", example = "john")
        String username,

        @Schema(
                description = "JWT token used for authenticated requests",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String token
) {}