package com.example.accessingdatajpa.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class ContactController {
    private final com.example.accessingdatajpa.service.ContactService contactService;

    public ContactController(com.example.accessingdatajpa.service.ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<com.example.accessingdatajpa.dto.ContactResponse> createCustomer(
            @Valid @RequestBody com.example.accessingdatajpa.dto.ContactRequest request) {

        com.example.accessingdatajpa.dto.ContactResponse created = contactService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public Iterable<com.example.accessingdatajpa.entity.Contact> getAllCustomers() {
        return contactService.getAll();
    }

    @GetMapping("/{id}")
    public com.example.accessingdatajpa.entity.Contact getCustomerById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @PutMapping("/{id}")
    public com.example.accessingdatajpa.entity.Contact updateCustomer(@PathVariable Long id, @RequestBody com.example.accessingdatajpa.entity.Contact customer) {
        return contactService.update(id, customer);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        contactService.delete(id);
    }
}
