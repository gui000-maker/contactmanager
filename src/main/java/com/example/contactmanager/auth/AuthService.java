package com.example.contactmanager.auth;

import com.example.contactmanager.entity.RefreshToken;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.JwtService;
import com.example.contactmanager.security.Role;
import com.example.contactmanager.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;

    }

    public AuthResponse login(AuthRequest request) {

        logger.debug("Attempting login for user: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenService.create(userDetails.getUsername());
        String token = jwtService.generateToken(userDetails.getUsername());

        logger.info("User logged in successfully: {}", userDetails.getUsername());

        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse register(RegisterRequest request) {

        logger.debug("Attempting to register user: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password())
        );

        user.setRole(Role.ROLE_USER);

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getUsername());

        RefreshToken refreshToken = refreshTokenService.create(saved.getUsername());

        logger.info("User successfully registered: {}", saved.getUsername());

        // register → immediately returns tokens
        return new AuthResponse(
                token,
                refreshToken.getToken()
        );
    }
}