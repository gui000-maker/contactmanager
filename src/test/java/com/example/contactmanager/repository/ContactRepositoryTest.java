package com.example.contactmanager.repository;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.security.Role;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private UserRepository userRepository; // ← add this

    private User testUser;

    @BeforeEach
    void setUp() {
        // must save user to DB — contacts reference it via foreign key
        User user = new User("alice", "password");
        user.setRole(Role.ROLE_USER);
        testUser = userRepository.save(user); // ← save and use the managed entity
    }

    @Test
    void shouldSaveAndFindContact() {
        Contact contact = new Contact("Alice", 30, "alice@example.com", "987654321", testUser);

        Contact saved = contactRepository.save(contact);
        Optional<Contact> found = contactRepository.findByIdAndUser(saved.getId(), testUser);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getAge()).isEqualTo(30);
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    void shouldReturnEmptyWhenContactNotFound() {
        Optional<Contact> found = contactRepository.findByIdAndUser(999L, testUser);
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCaseAndUser_shouldReturnMatches() {
        contactRepository.save(new Contact("Alice", 30, "alice@example.com", "987654321", testUser));

        Page<Contact> result = contactRepository
                .findByNameContainingIgnoreCaseAndUser("Alice", testUser, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findByNameContainingIgnoreCaseAndUser_shouldBeCaseInsensitive() {
        contactRepository.save(new Contact("Alice", 30, "alice@example.com", "987654321", testUser));

        Page<Contact> result = contactRepository
                .findByNameContainingIgnoreCaseAndUser("aLiCe", testUser, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findByNameContainingIgnoreCaseAndUser_shouldReturnEmptyWhenNoMatch() {
        Page<Contact> result = contactRepository
                .findByNameContainingIgnoreCaseAndUser("Bob", testUser, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByUser_shouldReturnOnlyUserContacts() {
        // save another user with their own contact
        User otherUser = new User("bob", "password");
        otherUser.setRole(Role.ROLE_USER);
        otherUser = userRepository.save(otherUser);

        contactRepository.save(new Contact("Alice", 30, "alice@example.com", "111", testUser));
        contactRepository.save(new Contact("Bob", 25, "bob@example.com", "222", otherUser));

        Page<Contact> result = contactRepository
                .findAllByUser(testUser, Pageable.unpaged());

        // alice only sees her own contact, not bob's
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void existsByIdAndUser_shouldReturnTrue_whenContactBelongsToUser() {
        Contact saved = contactRepository.save(
                new Contact("Alice", 30, "alice@example.com", "111", testUser));

        assertThat(contactRepository.existsByIdAndUser(saved.getId(), testUser)).isTrue();
    }

    @Test
    void existsByIdAndUser_shouldReturnFalse_whenContactBelongsToDifferentUser() {
        User otherUser = new User("bob", "password");
        otherUser.setRole(Role.ROLE_USER);
        otherUser = userRepository.save(otherUser);

        Contact saved = contactRepository.save(
                new Contact("Alice", 30, "alice@example.com", "111", otherUser));

        // alice cannot access bob's contact
        assertThat(contactRepository.existsByIdAndUser(saved.getId(), testUser)).isFalse();
    }
}
