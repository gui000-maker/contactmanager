package com.example.contactmanager.service;

import com.example.contactmanager.entity.RefreshToken;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.exception.TokenExpiredException;
import com.example.contactmanager.repository.RefreshTokenRepository;
import com.example.contactmanager.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class RefreshTokenServiceUnitTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    UserRepository userRepository;

    private RefreshTokenService refreshTokenService;
    private EntityManager entityManager;

    private long refreshExpiration;

    @BeforeEach
    void setUp() {
        refreshExpiration = 604800000L;
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                userRepository,
                entityManager,
                refreshExpiration
        );
    }

    @Test
    void create_shouldDeleteExistingTokenAndCreateNew() {
        User user = new User("alice", "password");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.create("alice");

        verify(refreshTokenRepository).deleteByUser(user); // old token deleted
        verify(refreshTokenRepository).save(any());        // new token saved
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.create("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validate_shouldReturnToken_whenValid() {
        User user = new User("alice", "password");
        RefreshToken token = new RefreshToken(user, "valid-token",
                Instant.now().plusMillis(86400000));

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validate("valid-token");

        assertThat(result.getToken()).isEqualTo("valid-token");
    }

    @Test
    void validate_shouldThrowTokenExpiredException_whenExpired() {
        User user = new User("alice", "password");
        RefreshToken token = new RefreshToken(user, "expired-token",
                Instant.now().minusMillis(1000)); // already expired

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validate("expired-token"))
                .isInstanceOf(TokenExpiredException.class);

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void validate_shouldThrowResourceNotFoundException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validate("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteToken_whenUserExists() {
        User user = new User("alice", "password");

        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.create("alice");

        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any());
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }
}
