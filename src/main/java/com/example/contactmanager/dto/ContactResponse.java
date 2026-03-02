package com.example.accessingdatajpa.dto;

import java.time.LocalDateTime;

public record ContactResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDateTime createdAt
) {}