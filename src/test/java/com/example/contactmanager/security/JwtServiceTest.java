package com.example.contactmanager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;
    private long expiration;

    @BeforeEach
    void setUp() {
        expiration = 86400000L;
        jwtService = new JwtService(
                "test-secret-key-must-be-at-least-32-characters",
                expiration
        );
    }

    @Test
    void generateToken_shouldContainUsername() {
        String token = jwtService.generateToken("Alice");
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("Alice");
    }

    @Test
    void isValid_shouldReturnTrue_whenTokenIsValid () {
        String token = jwtService.generateToken("Alice");
        assertThat(jwtService.isValid(token, "Alice")).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenUsernameDoesNotMatch () {
        String token = jwtService.generateToken("Alice");
        assertThat(jwtService.isValid(token, "Bob")).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenTokenIsBlank () {
        assertThat(jwtService.isValid("", "Alice")).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenTokenIsExpired () {
        JwtService expiredJwtService = new JwtService(
                "test-secret-key-must-be-at-least-32-characters",
                -1000L  // expired 1 second in the past
        );

        String token = expiredJwtService.generateToken("Alice");

        assertThat(expiredJwtService.isValid(token, "Alice")).isFalse();
    }
}
