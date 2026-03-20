package com.example.contactmanager.unit;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceUnitTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    void shouldCreateContact() {
        ContactRequest request = new ContactRequest(
                "John",
                25,
                "john@example.com",
                "123456789"
        );

        Contact contact = new Contact(
                request.name(),
                request.age(),
                request.email(),
                request.phoneNumber()
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
        when(contactRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_shouldReturnContact_whenContactExists() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321");
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        ContactResponse response = contactService.getById(1L);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.age()).isEqualTo(30);
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.phoneNumber()).isEqualTo("987654321");
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenContactDoesNotExist() {
        when(contactRepository.findById(99L)).thenReturn(Optional.empty());

        ContactRequest request = new ContactRequest("Bob", 35, "bob@example.com", "555555555");

        assertThatThrownBy(() -> contactService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenContactDoesNotExist() {
        when(contactRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> contactService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteContact_whenExists() {
        when(contactRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> contactService.delete(1L))
                .doesNotThrowAnyException();

        verify(contactRepository).deleteById(1L);
    }

    @Test
    void searchByName_shouldReturnEmptyPage_whenNameIsNotBlank() {
        Page<ContactResponse> result = contactService.searchByName("John", Pageable.unpaged());

        assertThat(result).isEmpty();
        // verify repository was never called — non-blank input should not short-circuit
        verifyNoInteractions(contactRepository);
    }

    @Test
    void searchByName_shouldReturnEmptyPage_whenNameIsBlank() {
        Page<ContactResponse> result = contactService.searchByName("  ", Pageable.unpaged());

        assertThat(result).isEmpty();
        // verify repository was never called — blank input should short-circuit
        verifyNoInteractions(contactRepository);
    }
}
