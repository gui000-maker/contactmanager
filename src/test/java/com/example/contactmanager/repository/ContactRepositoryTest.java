package com.example.contactmanager.repository;

import com.example.contactmanager.entity.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Test
    void shouldSaveAndFindContact() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321");

        Contact saved = contactRepository.save(contact);
        Optional<Contact> found = contactRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getAge()).isEqualTo(30);
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    void shouldReturnEmptyWhenContactNotFound() {
        Optional<Contact> found = contactRepository.findById(999L);
        assertThat(found).isEmpty();
    }
}