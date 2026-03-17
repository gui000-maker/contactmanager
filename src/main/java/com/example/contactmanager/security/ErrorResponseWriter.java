package com.example.contactmanager.security;

import com.example.contactmanager.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Utility class to write consistent API error responses.
 */
public class ErrorResponseWriter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ErrorResponseWriter() {}

    public static void write(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {

        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                LocalDateTime.now()
        );

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), error);
    }
}