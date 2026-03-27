package com.example.contactmanager.auth;

import com.example.contactmanager.entity.RefreshToken;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.JwtService;
import com.example.contactmanager.security.Role;
import com.example.contactmanager.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 * All dependencies are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        User user = new User("alice", "hashed");
        user.setRole(Role.ROLE_USER);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("alice")).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken(user, "refresh-token",
                Instant.now().plusMillis(86400000));
        when(refreshTokenService.create("alice")).thenReturn(refreshToken);

        AuthResponse response = authService.login(new AuthRequest("alice", "password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenPasswordIsWrong() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new AuthRequest("alice", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_shouldReturnTokens_whenUsernameIsAvailable() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashed");

        User saved = new User("alice", "hashed");
        saved.setRole(Role.ROLE_USER);
        when(userRepository.save(any())).thenReturn(saved);
        when(jwtService.generateToken("alice")).thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken(saved, "refresh-token",
                Instant.now().plusMillis(86400000));
        when(refreshTokenService.create("alice")).thenReturn(refreshToken);

        AuthResponse response = authService.register(
                new RegisterRequest("alice", "password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(passwordEncoder).encode("password"); // raw password was encoded
    }

    @Test
    void register_shouldThrowIllegalArgumentException_whenUsernameExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("alice", "password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        User user = new User("alice", "hashed");
        RefreshToken oldToken = new RefreshToken(user, "old-refresh",
                Instant.now().plusMillis(86400000));

        when(refreshTokenService.validate("old-refresh")).thenReturn(oldToken);
        when(jwtService.generateToken("alice")).thenReturn("new-access-token");

        RefreshToken newToken = new RefreshToken(user, "new-refresh",
                Instant.now().plusMillis(86400000));
        when(refreshTokenService.create("alice")).thenReturn(newToken);

        AuthResponse response = authService.refresh("old-refresh");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logout_shouldDeleteRefreshToken_whenTokenIsValid() {
        User user = new User("alice", "hashed");
        RefreshToken token = new RefreshToken(user, "refresh-token",
                Instant.now().plusMillis(86400000));

        when(refreshTokenService.validate("refresh-token")).thenReturn(token);

        assertThatCode(() -> authService.logout("refresh-token"))
                .doesNotThrowAnyException();

        verify(refreshTokenService).delete("alice"); // token was deleted
    }
}
