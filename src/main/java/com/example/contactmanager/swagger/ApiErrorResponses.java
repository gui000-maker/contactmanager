package com.example.contactmanager.swagger;

import com.example.contactmanager.exception.ApiError;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

import java.lang.annotation.*;

/**
 * Composite Swagger annotation that documents standard error responses
 * shared across all secured endpoints in this API.
 *
 * <p>Apply at method or class level on controllers to avoid repeating
 * error response definitions on every endpoint.</p>
 *
 * <p>Documented responses:</p>
 * <ul>
 *   <li>400 - Validation failed (e.g. invalid email format)</li>
 *   <li>401 - No valid JWT token provided</li>
 *   <li>403 - Authenticated but insufficient permissions</li>
 *   <li>404 - Requested resource not found</li>
 *   <li>409 - Resource conflict (e.g. duplicate contact)</li>
 *   <li>500 - Unexpected server error</li>
 * </ul>
 *
 * <p><b>Note:</b> This annotation is documentation only. Runtime behavior
 * for 401/403 is handled by {@link com.example.contactmanager.security.SecurityConfig}
 * via the authentication entry point and access denied handler.
 * All other errors are handled by the global exception handler.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * {@literal @}ApiErrorResponses
 * {@literal @}RequestMapping("/contacts")
 *  public class ContactController { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({

        @ApiResponse(
                responseCode = "400",
                description = "Bad Request - Validation error",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "BadRequestExample",
                                value = """
                                {
                                  "status": 400,
                                  "error": "Bad Request",
                                  "message": "email: must be a valid email",
                                  "path": "/contacts",
                                  "timestamp": "2026-03-15T20:15:30"
                                }
                                """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "UnauthorizedExample",
                                value = """
                                {
                                  "status": 401,
                                  "error": "Unauthorized",
                                  "message": "Authentication required",
                                  "path": "/contacts",
                                  "timestamp": "2026-03-15T20:15:30"
                                }
                                """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "403",
                description = "Forbidden - Insufficient permissions",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "ForbiddenExample",
                                value = """
            {
              "status": 403,
              "error": "Forbidden",
              "message": "You do not have permission to access this resource",
              "path": "/contacts",
              "timestamp": "2026-03-15T20:15:30"
            }
            """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "404",
                description = "Resource not found",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "NotFoundExample",
                                value = """
                                {
                                  "status": 404,
                                  "error": "Not Found",
                                  "message": "Contact not found with id: 10",
                                  "path": "/contacts/10",
                                  "timestamp": "2026-03-15T20:15:30"
                                }
                                """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "409",
                description = "Conflict",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "ConflictExample",
                                value = """
                                {
                                  "status": 409,
                                  "error": "Conflict",
                                  "message": "Contact already exists",
                                  "path": "/contacts",
                                  "timestamp": "2026-03-15T20:15:30"
                                }
                                """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "429",
                description = "Too Many Requests — rate limit exceeded",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "TooManyRequestsExample",
                                value = """
                        {
                          "status": 429,
                          "error": "Too Many Requests",
                          "message": "Too many login attempts. Try again in 1 minute.",
                          "path": "/api/auth/login",
                          "timestamp": "2026-03-27T16:00:00"
                        }
                        """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                        schema = @Schema(implementation = ApiError.class),
                        examples = @ExampleObject(
                                name = "ServerErrorExample",
                                value = """
                                {
                                  "status": 500,
                                  "error": "Internal Server Error",
                                  "message": "Unexpected server error",
                                  "path": "/contacts",
                                  "timestamp": "2026-03-15T20:15:30"
                                }
                                """
                        )
                )
        )
})
public @interface ApiErrorResponses {}