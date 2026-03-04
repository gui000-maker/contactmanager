package com.example.contactmanager.dto;

import jakarta.validation.constraints.*;

/**
 * DTO used for creating/updating a Customer.
 */
public record ContactRequest(

        @NotBlank(message = "Name cannot be empty")
        @Size(max = 50, message = "Name must be at most 50 characters")
        String name,

        @Min(value = 0, message = "Age must be >= 0")
        @Max(value = 120, message = "Age must be <= 120")
        Integer age,

        @Email(message = "Email must be valid")
        String email,

        @Pattern(regexp = "\\+?[0-9\\-]{7,15}", message = "Invalid phone number")
        String phoneNumber
) {}