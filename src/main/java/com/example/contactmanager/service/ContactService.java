package com.example.accessingdatajpa.service;

import org.springframework.stereotype.Service;

@Service
public class ContactService {
    private final com.example.accessingdatajpa.repository.ContactRepository contactRepository;

    public ContactService(com.example.accessingdatajpa.repository.ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public com.example.accessingdatajpa.dto.ContactResponse create(com.example.accessingdatajpa.dto.ContactRequest request) {

        com.example.accessingdatajpa.entity.Contact customer = new com.example.accessingdatajpa.entity.Contact(
                request.firstName(),
                request.lastName()
        );

        com.example.accessingdatajpa.entity.Contact saved = contactRepository.save(customer);

        return new com.example.accessingdatajpa.dto.ContactResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getCreatedAt()
        );
    }

    public Iterable<com.example.accessingdatajpa.entity.Contact> getAll() {
        return contactRepository.findAll();
    }

    public com.example.accessingdatajpa.entity.Contact getById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public com.example.accessingdatajpa.entity.Contact update(Long id, com.example.accessingdatajpa.entity.Contact updatedCustomer) {
        com.example.accessingdatajpa.entity.Contact customer = getById(id);
        customer.setFirstName(updatedCustomer.getFirstName());
        customer.setLastName(updatedCustomer.getLastName());
        return contactRepository.save(customer);
    }

    public void delete(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        contactRepository.deleteById(id);
    }
}
