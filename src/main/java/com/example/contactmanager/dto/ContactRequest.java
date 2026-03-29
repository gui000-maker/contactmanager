package com.example.contactmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "ContactRequest", description = "Request to create a new contact")
public record ContactRequest(

        @Schema(description = "Name of the contact", example = "John Doe")
        @NotBlank(message = "Name cannot be empty")
        @Size(max = 50, message = "Name must be at most 50 characters")
        String name,

        @Schema(description = "Age of the contact", example = "25")
        @Min(value = 0, message = "Age must be >= 0")
        @Max(value = 120, message = "Age must be <= 120")
        Integer age,

        @Schema(description = "Email of the contact", example = "john@example.com")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Phone number of the contact", example = "+123456789")
        @Pattern(regexp = "\\+?[0-9\\-]{7,15}", message = "Invalid phone number")
        String phoneNumber
) {}
