package com.example.contactmanager.auth;

import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(UserRequest request) {

        logger.debug("Attempting to register user: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password())
        );

        User saved = userRepository.save(user);

        logger.info("User successfully registered: {}", saved.getUsername());

        return new AuthResponse(
                saved.getUsername(),
                "User registered successfully"
        );
    }
}