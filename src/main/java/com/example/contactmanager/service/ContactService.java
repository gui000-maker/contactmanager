package com.example.contactmanager.service;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {

        logger.info("Creating contact with email: {}", request.email());

        Contact contact = new Contact(
                request.name(),
                request.age(),
                request.email(),
                request.phoneNumber()
        );

        Contact saved = contactRepository.save(contact);

        logger.info("Contact created with id: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> getAll(Pageable pageable) {

        logger.debug("Fetching all contacts with pageable: {}", pageable);

        return contactRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {

        logger.debug("Fetching contact with id: {}", id);

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        return toResponse(contact);
    }

    @Transactional
    public ContactResponse update(Long id, ContactRequest updatedContact) {

        logger.info("Updating contact with id: {}", id);

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        contact.setName(updatedContact.name());
        contact.setAge(updatedContact.age());
        contact.setEmail(updatedContact.email());
        contact.setPhoneNumber(updatedContact.phoneNumber());

        return toResponse(contact);
    }

    @Transactional
    public void delete(Long id) {

        logger.info("Deleting contact with id: {}", id);

        if (!contactRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact not found with id: " + id);
        }

        contactRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> searchByName(String name, Pageable pageable) {

        logger.debug("Searching for contacts with name: {}", name);

        if (name == null || name.isBlank()) {
            return Page.empty(pageable);
        }

        return contactRepository
                .findByNameContainingIgnoreCase(name.trim(), pageable)
                .map(this::toResponse);
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getAge(),
                contact.getEmail(),
                contact.getPhoneNumber(),
                contact.getCreatedAt()
        );
    }
}