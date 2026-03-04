package com.example.contactmanager.repository;

import com.example.contactmanager.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
