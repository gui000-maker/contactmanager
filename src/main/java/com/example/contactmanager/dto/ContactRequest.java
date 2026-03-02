package com.example.accessingdatajpa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used for creating/updating a Customer.
 */
public record ContactRequest(

        @NotBlank(message = "First name cannot be empty")
        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName
) {}