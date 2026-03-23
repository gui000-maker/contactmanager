package com.example.contactmanager.integration;

import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.JwtService;
import com.example.contactmanager.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // create a regular user
        User user = new User("alice", passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        // create an admin user
        User admin = new User("admin", passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }

    @Test
    void shouldReturn401_whenNoAuthHeader() throws Exception {
        mockMvc.perform(get("/contacts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenTokenIsMalformed() throws Exception {
        mockMvc.perform(get("/contacts")
                        .header("Authorization", "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenTokenIsExpired() throws Exception {
        JwtService expiredJwtService = new JwtService(
                "test-secret-key-must-be-at-least-32-characters",
                -1000L
        );
        String expiredToken = expiredJwtService.generateToken("alice");

        mockMvc.perform(get("/contacts")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403_whenValidTokenButWrongRole() throws Exception {
        // alice is ROLE_USER — /users requires ROLE_ADMIN
        String token = jwtService.generateToken("alice");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn200_whenValidToken() throws Exception {
        String token = jwtService.generateToken("alice");

        mockMvc.perform(get("/contacts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200_whenAdminAccessesAdminEndpoint() throws Exception {
        String token = jwtService.generateToken("admin");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPermitSwaggerWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPermitAuthEndpointWithoutToken() throws Exception {
        var requestJson = """
            {"username": "alice", "password": "password"}
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}