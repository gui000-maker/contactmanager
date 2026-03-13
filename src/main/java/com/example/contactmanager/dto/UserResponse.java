package com.example.contactmanager.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String password,
        LocalDateTime createdAt
) {}
