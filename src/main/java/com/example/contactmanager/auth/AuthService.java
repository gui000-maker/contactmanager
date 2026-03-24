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

/**
 * Service responsible for authentication operations — register, login,
 * token refresh and logout.
 *
 * <p>On both register and login, two tokens are issued:</p>
 * <ul>
 *   <li>Access token — short lived JWT for authenticated requests</li>
 *   <li>Refresh token — long lived opaque token stored in the database</li>
 * </ul>
 *
 * <p>Refresh tokens are rotated on every use — the old token is deleted
 * and a new one is issued. This limits the damage of a stolen refresh token.</p>
 */
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

    /**
     * Authenticates the user and issues an access token and refresh token.
     *
     * @param request login credentials
     * @return access token and refresh token
     * @throws org.springframework.security.authentication.BadCredentialsException
     *         if the username or password is incorrect — maps to 401
     */
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

    /**
     * Registers a new user and immediately issues tokens.
     *
     * <p>The user is logged in automatically after registration —
     * no separate login call is needed.</p>
     *
     * <p>New users are always assigned {@link Role#ROLE_USER}.
     * Admin accounts cannot be created through this endpoint.</p>
     *
     * @param request registration details
     * @return access token and refresh token
     * @throws IllegalArgumentException if the username is already taken — maps to 409
     */
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

        return new AuthResponse(token, refreshToken.getToken());
    }

    /**
     * Validates the given refresh token and issues a new access token
     * and refresh token (rotation).
     *
     * @param refreshToken the refresh token string from the client
     * @return new access token and new refresh token
     * @throws com.example.contactmanager.exception.TokenExpiredException
     *         if the refresh token has expired — maps to 401
     */
    public AuthResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenService.validate(refreshToken);
        String username = token.getUser().getUsername();
        String newAccessToken = jwtService.generateToken(username);
        RefreshToken newRefreshToken = refreshTokenService.create(username);
        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    /**
     * Invalidates the refresh token, effectively logging the user out.
     *
     * <p>The access token is not invalidated — it remains valid until
     * it expires naturally. The client should discard it immediately.</p>
     *
     * @param refreshToken the refresh token string to invalidate
     */
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.validate(refreshToken);
        refreshTokenService.delete(token.getUser().getUsername());
    }
}