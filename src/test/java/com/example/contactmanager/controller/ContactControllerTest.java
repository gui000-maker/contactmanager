package com.example.contactmanager.controller;

import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContactControllerUnitTest {

    private MockMvc mockMvc;
    private ContactService contactService; // mock service

    @BeforeEach
    void setup() {
        contactService = mock(ContactService.class);
        ContactController controller = new ContactController(contactService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createContactShouldReturnCreated() throws Exception {
        var requestJson = """
            {"name": "John", "email": "john@example.com", "age": 25, "phoneNumber": "123456789"}
            """;

        var response = new ContactResponse(1L, "John", 25, "john@example.com",  "123456789", LocalDateTime.now());
        when(contactService.create(any())).thenReturn(response);

        mockMvc.perform(post("/contacts")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"));
    }
}