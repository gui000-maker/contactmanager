package com.example.contactmanager.service;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public ContactResponse create(ContactRequest request) {

        Contact contact = new Contact(
                request.name(),
                request.age(),
                request.email(),
                request.phoneNumber()
        );

        Contact saved = contactRepository.save(contact);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> getAll(Pageable pageable) {

        return contactRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        return toResponse(contact);
    }

    public ContactResponse update(Long id, ContactRequest updatedContact) {

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

    public void delete(Long id) {

        if (!contactRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact not found with id: " + id);
        }

        contactRepository.deleteById(id);
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