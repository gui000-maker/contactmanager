package com.example.contactmanager.service;

import com.example.contactmanager.entity.RefreshToken;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.exception.TokenExpiredException;
import com.example.contactmanager.repository.RefreshTokenRepository;
import com.example.contactmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages refresh token lifecycle — creation, validation and rotation.
 * Tokens are stored in the database and rotated on every use.
 * One user can only have one active refresh token at a time.
 */
@Service
@Transactional
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new refresh token for the user.
     * Deletes any existing token first — one token per user.
     */
    public RefreshToken create(String username) {
        logger.info("Creating refresh token for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // delete existing refresh token if it exists
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken(user,
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validates a refresh token.
     * Throws if the token does not exist or is expired.
     */
    public RefreshToken validate(String token) {
        logger.info("Validating refresh token for request");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found with token: " + token));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired — please log in again");
        }

        return refreshToken;
    }

    /**
     * Deletes the refresh token for the given user.
     * Called on logout.
     */
    public void delete(String username) {
        logger.info("Deleting refresh token for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        refreshTokenRepository.deleteByUser(user);
    }
}
