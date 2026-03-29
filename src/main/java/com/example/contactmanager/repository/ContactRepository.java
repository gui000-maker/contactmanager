package com.example.contactmanager.repository;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findAllByUser(User user, Pageable pageable);

    Optional<Contact> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    Page<Contact> findByNameContainingIgnoreCase(String name, Pageable pageable);

    void deleteByIdAndUser(Long id, User user);

    Page<Contact> findByNameContainingIgnoreCaseAndUser(String name, User user, Pageable pageable);
}
