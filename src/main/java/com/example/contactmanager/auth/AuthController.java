package com.example.contactmanager.auth;

import com.example.contactmanager.dto.*;
import com.example.contactmanager.swagger.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ApiErrorResponses
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Auth operations")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}