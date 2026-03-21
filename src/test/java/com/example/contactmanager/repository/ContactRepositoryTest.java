package com.example.contactmanager.repository;

import com.example.contactmanager.entity.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
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

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatches() {
        contactRepository.save(new Contact("Alice", 30, "alice@example.com", "987654321"));

        Page<Contact> result = contactRepository
                .findByNameContainingIgnoreCase("Alice", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findByNameContainingIgnoreCase_shouldBeCaseInsensitive() {
        Contact alice = new Contact("Alice", 30, "alice@example.com", "987654321");
        contactRepository.save(alice);
        assertThat(contactRepository.findByNameContainingIgnoreCase("aLiCe", Pageable.unpaged()))
                .hasSize(1)
                .contains(alice);
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnEmptyWhenNoMatch() {
        assertThat(contactRepository.findByNameContainingIgnoreCase("Bob", Pageable.unpaged()))
                .isEmpty();
    }
}