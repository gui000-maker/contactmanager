package com.example.contactmanager.controller;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.service.ContactService;
import com.example.contactmanager.swagger.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for contact management.
 *
 * <p>All endpoints require authentication. Error response format
 * is standardized via {@link com.example.contactmanager.swagger.ApiErrorResponses}
 * and handled at runtime by the global exception handler and
 * security config entry points.</p>
 */
@ApiErrorResponses
@RestController
@RequestMapping("/contacts")
@Tag(name = "Contacts", description = "Operations related to contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Create a new contact")
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody ContactRequest request
    ) {
        ContactResponse created = contactService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @Operation(summary = "Get all contacts")
    @GetMapping
    public Page<ContactResponse> getAllContacts(Pageable pageable) {
        return contactService.getAll(pageable);
    }

    @Operation(summary = "Get a contact by ID")
    @GetMapping("/{id}")
    public ContactResponse getContactById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @Operation(summary = "Update a contact by ID")
    @PutMapping("/{id}")
    public ContactResponse updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request
    ) {
        return contactService.update(id, request);
    }

    @Operation(summary = "Delete a contact by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.delete(id);
    }

    @Operation(summary = "Search for contacts by name")
    @GetMapping("/search")
    public Page<ContactResponse> searchContacts(
            @RequestParam String name,
            Pageable pageable
    ) {
        return contactService.searchByName(name, pageable);
    }
}
