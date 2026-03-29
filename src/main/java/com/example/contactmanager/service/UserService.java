package com.example.contactmanager.service;

import com.example.contactmanager.dto.UserResponse;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.contactmanager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic layer for user management.
 *
 * <p>Handles user creation, retrieval and deletion. Password hashing
 * is performed here before persistence — raw passwords never reach
 * the repository or database.</p>
 *
 * <p>All methods throw {@link ResourceNotFoundException} when a user
 * is not found, which maps to a 404 response via the global exception handler.</p>
 *
 * <p>Note: these operations are restricted to ROLE_ADMIN at the
 * controller level via {@literal @}PreAuthorize.</p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates and persists a new user with a hashed password.
     *
     * <p>New users are always assigned {@link Role#ROLE_USER} by default.
     * There is no endpoint to create admin users — role elevation
     * must be done directly in the database.</p>
     *
     * @param request the username and raw password to create
     * @return the saved user as a response DTO (password not included)
     * @throws IllegalArgumentException if the username is already taken
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        logger.info("Creating user with username: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password())// raw password is hashed here, never stored plain
        );

        user.setRole(Role.ROLE_USER);

        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    /**
     * Returns a paginated list of all users.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of user response DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        logger.debug("Fetching all users with pageable: {}", pageable);
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Returns a single user by ID.
     *
     * @param id the user ID
     * @return the matching user as a response DTO
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        logger.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id)
                );

        return toResponse(user);
    }

    /**
     * Deletes a user by ID.
     *
     * <p>Existence is checked before deletion to throw a meaningful
     * exception rather than silently doing nothing, which is the
     * default behavior of {@code deleteById()} when the ID is not found.</p>
     *
     * @param id the ID of the user to delete
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    /**
     * Converts a {@link User} entity to a {@link UserResponse} DTO.
     * Password is intentionally excluded from the response.
     *
     * @param user the entity to convert
     * @return the mapped response DTO
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
