package com.example.contactmanager.unit;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
}
