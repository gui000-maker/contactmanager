package com.example.contactmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized exception handler for all controllers in the application.
 *
 * <p>Maps application and Spring exceptions to consistent {@link ApiError}
 * JSON responses. Works alongside {@link com.example.contactmanager.security.ErrorResponseWriter}
 * which handles 401/403 errors that occur in the security filter chain —
 * before requests ever reach this handler.</p>
 *
 * <p>Exception mappings:</p>
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request</li>
 *   <li>{@link IllegalArgumentException} → 409 Conflict</li>
 *   <li>{@link UsernameNotFoundException} → 401 Unauthorized</li>
 *   <li>{@link BadCredentialsException} → 401 Unauthorized</li>
 *   <li>{@link Exception} → 500 Internal Server Error (catch-all)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles requests for resources that do not exist.
     *
     * @param ex      the exception carrying the not-found message
     * @param request the current HTTP request, used to include the path in the response
     * @return a 404 {@link ApiError} response body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        logger.warn("Resource not found: {}", ex.getMessage());

        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handles validation failures from {@literal @}Valid on request bodies.
     *
     * <p>Collects all field errors into a single comma-separated message.
     * Example: {@code "email: must be a valid email, name: must not be blank"}</p>
     *
     * @param ex      the exception containing all field validation errors
     * @param request the current HTTP request
     * @return a 400 {@link ApiError} response body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        logger.warn("Validation error: {}", message);

        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }

    /**
     * Handles business rule conflicts such as duplicate usernames or contacts.
     *
     * <p>Note: {@link IllegalArgumentException} is a broad exception type.
     * Only throw it for conflict scenarios in this application — if other
     * uses are added, consider creating a dedicated exception class instead
     * to avoid unintended 409 responses.</p>
     *
     * @param ex      the exception carrying the conflict message
     * @param request the current HTTP request
     * @return a 409 {@link ApiError} response body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logger.warn("Conflict error: {}", ex.getMessage());

        return new ApiError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handles cases where the username provided during login does not exist.
     *
     * <p>Returns a generic 401 rather than a specific "user not found" message
     * to avoid confirming whether a username exists in the system
     * (user enumeration prevention).</p>
     *
     * @param ex      the Spring Security exception
     * @param request the current HTTP request
     * @return a 401 {@link ApiError} response body
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request
    ) {
        logger.warn("Authentication error: {}", ex.getMessage());

        return new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid username or password",
                request.getRequestURI()
        );
    }

    /**
     * Handles failed login attempts due to wrong password.
     *
     * <p>The original exception message is intentionally not used —
     * a generic response is returned to avoid leaking whether the
     * username or the password was wrong (credential enumeration prevention).</p>
     *
     * @param ex      the Spring Security exception
     * @param request the current HTTP request
     * @return a 401 {@link ApiError} response body
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        logger.warn("Authentication failed: {}", ex.getMessage());

        return new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid username or password", // generic message — intentional
                request.getRequestURI()
        );
    }

    /**
     * Catch-all handler for any unhandled exception.
     *
     * <p>Logs the full stack trace for debugging but returns a generic message
     * to the client — internal details such as class names, stack frames
     * or database errors are never exposed in the response.</p>
     *
     * @param ex      the unhandled exception
     * @param request the current HTTP request
     * @return a 500 {@link ApiError} response body
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.error("Unexpected error occurred", ex); // full stack trace logged here

        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred", // generic message — stack trace never sent to client
                request.getRequestURI()
        );
    }

    /**
     * Handles expired refresh tokens.
     * Returns 401 so the client knows to redirect to login.
     */
    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest request
    ) {
        logger.warn("Expired token: {}", ex.getMessage());

        return new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Formats a single field validation error into a readable string.
     * Example output: {@code "email: must be a valid email"}
     *
     * @param error the field error from the binding result
     * @return a formatted string combining the field name and error message
     */
    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
