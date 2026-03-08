package com.example.contactmanager.controller;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
@Tag(name = "Contacts", description = "Operations related to contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @Operation(summary = "Create a new contact")
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse created = contactService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    @Operation(summary = "Get all contacts")
    public Page<ContactResponse> getAllContacts(@ParameterObject Pageable pageable) {
        return contactService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contact by ID")
    public ContactResponse getContactById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contact by ID")
    public ContactResponse updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request
    ) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contact by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.delete(id);
    }
}
