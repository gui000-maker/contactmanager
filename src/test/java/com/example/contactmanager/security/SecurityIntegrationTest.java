package com.example.contactmanager.security;

import com.example.contactmanager.entity.User;
import com.example.contactmanager.repository.RefreshTokenRepository;
import com.example.contactmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter.clearBuckets();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User("alice", passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "alice", "password": "password"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200_whenRefreshingWithValidToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "alice", "password": "password"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = new ObjectMapper()
                .readTree(loginResponse)
                .get("refreshToken")
                .asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"refreshToken": "%s"}
                                """, refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldReturn404_whenRefreshingWithNonExistentToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "00000000-0000-0000-0000-000000000000"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204_whenLoggingOutWithValidToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "alice", "password": "password"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = new ObjectMapper()
                .readTree(loginResponse)
                .get("refreshToken")
                .asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"refreshToken": "%s"}
                                """, refreshToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn429_whenLoginExceedsRateLimit() throws Exception {
        String loginJson = """
                {"username": "alice", "password": "password"}
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldReturnEmptyContacts_whenUserHasNoContacts() throws Exception {
        String token = jwtService.generateToken("alice");

        mockMvc.perform(get("/contacts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturn404_whenAccessingAnotherUsersContact() throws Exception {
        // admin creates a contact
        String adminToken = jwtService.generateToken("admin");

        String createResponse = mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("""
                                {"name": "John", "age": 25, "email": "john@example.com", "phoneNumber": "123456789"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long contactId = new ObjectMapper()
                .readTree(createResponse)
                .get("id")
                .asLong();

        // alice tries to access admin's contact — should get 404
        String aliceToken = jwtService.generateToken("alice");
        mockMvc.perform(get("/contacts/" + contactId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNotFound());
    }
}