package com.example.contactmanager.swagger;

import com.example.contactmanager.exception.ApiError;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

import java.lang.annotation.*;

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