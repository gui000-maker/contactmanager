package com.example.contactmanager.controller;

import com.example.contactmanager.dto.UserResponse;
import com.example.contactmanager.exception.GlobalExceptionHandler;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.security.Role;
import com.example.contactmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


class UserControllerUnitTest {

    private MockMvc mockMvc;
    private UserService userService; // mock service

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(new LocalValidatorFactoryBean())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        var requestJson = """
            {"username": "Alice", "password": "password"}
            """;

        var response =  new UserResponse(1L, "Alice", Role.ROLE_USER, LocalDateTime.now());
        when(userService.createUser(any())).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void createUserShouldReturnBadRequest_whenInvalidRequest() throws Exception {
        var requestJson = """
            {"username": "Alice", "password": ""}
            """;

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturn404_whenNotFound() throws Exception {
        when(userService.findById(999L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }
}
