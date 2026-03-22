package com.example.contactmanager.service;

import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.dto.UserResponse;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        UserRequest request = new UserRequest("Alice", "password");

        when(passwordEncoder.encode("password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(new User("Alice", "hashed-password"));

        UserResponse response = userService.createUser(request);

        assertThat(response.username()).isEqualTo("Alice");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);
        verify(passwordEncoder).encode("password");  // ← raw password was encoded
        verify(userRepository).save(any(User.class));
    }
}