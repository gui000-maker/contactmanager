package com.example.contactmanager.service;

import com.example.contactmanager.dto.UserRequest;
import com.example.contactmanager.dto.UserResponse;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
        User expectedUser = new User("Alice", "hashed-password");
        expectedUser.setRole(Role.ROLE_USER);

        when(passwordEncoder.encode("password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.username()).isEqualTo("Alice");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);
        verify(passwordEncoder).encode("password");  // ← raw password was encoded
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenNotExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = new User("Alice", "hashed-password");
        user.setRole(Role.ROLE_USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response.username()).isEqualTo("Alice");
        assertThat(response.role()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void createUser_shouldThrowIllegalArgumentException_whenUsernameExists() {
        when(userRepository.existsByUsername("Alice")).thenReturn(true);

        UserRequest request = new UserRequest("Alice", "password");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException_whenNotExists() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteUser_shouldDeleteUser_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> userService.deleteUser(1L))
                .doesNotThrowAnyException();

        verify(userRepository).deleteById(1L);
    }

    @Test
    void getAll_shouldReturnPageOfUsers() {
        User user = new User("Alice", "hashed-password");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserResponse> result = userService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("Alice");
        verify(userRepository).findAll(any(Pageable.class));
    }
}