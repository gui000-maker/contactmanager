package com.example.contactmanager.dto;

import jakarta.validation.constraints.*;

public record UserRequest (

        @NotBlank(message = "Username cannot be empty")
        @Size(max = 50, message = "Username must be at most 50 characters")
        String username,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
