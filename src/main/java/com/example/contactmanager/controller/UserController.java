package com.example.contactmanager.controller;

import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.dto.UserResponse;
import com.example.contactmanager.service.UserService;
import com.example.contactmanager.swagger.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management.
 *
 * <p>All endpoints are restricted to users with ROLE_ADMIN,
 * enforced via {@literal @}PreAuthorize at the class level.
 * Non-admin authenticated users will receive a 403 response.</p>
 *
 * <p>Error response format is standardized via
 * {@link com.example.contactmanager.swagger.ApiErrorResponses}
 * and handled at runtime by the global exception handler and
 * security config entry points.</p>
 */
@PreAuthorize("hasRole('ADMIN')")
@ApiErrorResponses
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations related to users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request
    ) {
        UserResponse created = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @Operation(summary = "Get a user by ID")
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Delete a user by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
