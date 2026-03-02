package com.example.contactmanager.service;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;

import org.springframework.stereotype.Service;


@Service
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

        return new ContactResponse(
                saved.getId(),
                saved.getName(),
                saved.getAge(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getCreatedAt()
        );
    }

    public Iterable<Contact> getAll() {
        return contactRepository.findAll();
    }

    public Contact getById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    public Contact update(Long id, Contact updatedContact) {
        Contact contact = getById(id);
        contact.setName(updatedContact.getName());
        contact.setAge(updatedContact.getAge());
        contact.setEmail(updatedContact.getEmail());
        contact.setPhoneNumber(updatedContact.getPhoneNumber());
        return contactRepository.save(contact);
    }

    public void delete(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Contact not found");
        }
        contactRepository.deleteById(id);
    }
}
