package com.example.accessingdatajpa.dto;

import java.time.LocalDateTime;

public record ContactResponse(
        Long id,
        String name,
        int age,
        String email,
        String phoneNumber,
        LocalDateTime createdAt
) {}