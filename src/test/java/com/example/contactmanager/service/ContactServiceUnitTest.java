package com.example.contactmanager.service;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.repository.UserRepository;
import com.example.contactmanager.security.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ContactServiceUnitTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("alice", "password");
        testUser.setRole(Role.ROLE_USER);

        // mock SecurityContextHolder so getCurrentUser() works
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("alice");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // mock user lookup for getCurrentUser()
        lenient().when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(testUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateContact() {
        ContactRequest request = new ContactRequest(
                "John", 25, "john@example.com", "123456789"
        );

        Contact contact = new Contact(
                request.name(),
                request.age(),
                request.email(),
                request.phoneNumber(),
                testUser
        );

        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        ContactResponse response = contactService.create(request);

        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.age()).isEqualTo(request.age());
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.phoneNumber()).isEqualTo(request.phoneNumber());
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenContactDoesNotExist() {
        when(contactRepository.findByIdAndUser(99L, testUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_shouldReturnContact_whenContactExists() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321", testUser);
        when(contactRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(contact));

        ContactResponse response = contactService.getById(1L);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.age()).isEqualTo(30);
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.phoneNumber()).isEqualTo("987654321");
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenContactDoesNotExist() {
        when(contactRepository.findByIdAndUser(99L, testUser))
                .thenReturn(Optional.empty());

        ContactRequest request = new ContactRequest("Bob", 35, "bob@example.com", "555555555");

        assertThatThrownBy(() -> contactService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenContactDoesNotExist() {
        assertThatThrownBy(() -> contactService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteContact_whenExists() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321", testUser);

        when(contactRepository.findByIdAndUser(1L, testUser))
                .thenReturn(Optional.of(contact));

        assertThatCode(() -> contactService.delete(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void searchByName_shouldCallRepository_whenNameIsNotBlank() {
        when(contactRepository.findByNameContainingIgnoreCaseAndUser(
                eq("John"), eq(testUser), any(Pageable.class)))
                .thenReturn(Page.empty());

        contactService.searchByName("John", Pageable.unpaged());

        verify(contactRepository).findByNameContainingIgnoreCaseAndUser(
                eq("John"), eq(testUser), any(Pageable.class));
    }

    @Test
    void searchByName_shouldReturnEmptyPage_whenNameIsBlank() {
        Page<ContactResponse> result = contactService.searchByName("  ", Pageable.unpaged());

        assertThat(result).isEmpty();
        verifyNoInteractions(contactRepository);
    }

    @Test
    void getAll_shouldReturnOnlyCurrentUserContacts() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321", testUser);
        when(contactRepository.findAllByUser(eq(testUser), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(contact)));

        Page<ContactResponse> result = contactService.getAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Alice");
        verify(contactRepository).findAllByUser(eq(testUser), any(Pageable.class));
    }
}
