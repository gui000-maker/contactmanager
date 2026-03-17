package com.example.contactmanager.auth;

import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(UserRequest request) {

        logger.debug("Attempting login for user: {}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.username());

        logger.info("User logged in successfully: {}", request.username());

        return new AuthResponse(request.username(), token);
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

        user.setRole("USER");

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getUsername());

        logger.info("User successfully registered: {}", saved.getUsername());

        return new AuthResponse(
                saved.getUsername(),
                token
        );
    }
}