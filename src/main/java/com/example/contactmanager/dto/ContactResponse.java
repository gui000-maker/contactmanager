package com.example.contactmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "ContactResponse", description = "Response for a contact")
public record ContactResponse(

        @Schema(description = "ID of the contact", example = "1")
        Long id,

        @Schema(description = "Name of the contact", example = "John Doe")
        String name,

        @Schema(description = "Age of the contact", example = "25")
        int age,

        @Schema(description = "Email of the contact", example = "john@example.com")
        String email,

        @Schema(description = "Phone number of the contact", example = "+123456789")
        String phoneNumber,

        @Schema(description = "Creation date of the contact", example = "2023-07-25T12:00:00")
        LocalDateTime createdAt
) {}
