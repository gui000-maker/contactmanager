package com.example.contactmanager.controller;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse created = contactService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ContactResponse getContactById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request
    ) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.delete(id);
    }
}
