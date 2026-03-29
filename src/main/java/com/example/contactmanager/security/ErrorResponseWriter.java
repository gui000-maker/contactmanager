package com.example.contactmanager.security;

import com.example.contactmanager.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Utility component that writes a JSON {@link ApiError} directly
 * to an {@link HttpServletResponse}.
 *
 * <p>Used exclusively by the security config entry points (401/403)
 * which operate in the filter chain — before requests reach any
 * controller — and therefore cannot rely on
 * {@literal @}RestControllerAdvice to format error responses.</p>
 *
 * <p>All other application errors (404, 409, 500, etc.) are handled
 * by the global exception handler instead.</p>
 */
@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Writes a JSON error response directly to the HTTP response.
     *
     * <p>Sets the status code, content type and encoding before
     * writing the body — calling code must not write to the response
     * before or after this method, as doing so will cause an
     * {@link IllegalStateException} from writing to a committed response.</p>
     *
     * @param response the HTTP response to write to
     * @param status   the HTTP status code to return
     * @param message  a human-readable description of the error
     * @param path     the request URI where the error occurred
     * @throws IOException if writing to the response fails
     */
    public void write(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {

        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(), // e.g. "Unauthorized", "Forbidden"
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
