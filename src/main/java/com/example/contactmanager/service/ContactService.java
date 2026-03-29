package com.example.contactmanager.service;

import com.example.contactmanager.entity.Contact;
import com.example.contactmanager.entity.User;
import com.example.contactmanager.exception.ResourceNotFoundException;
import com.example.contactmanager.repository.ContactRepository;
import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic layer for contact management.
 *
 * <p>All write operations are transactional. Read operations use
 * {@code readOnly = true} for performance — this allows the database
 * to skip dirty checking and optimize read queries.</p>
 *
 * <p>All methods throw {@link ResourceNotFoundException} when a contact
 * is not found, which maps to a 404 response via the global exception handler.</p>
 */
@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates and persists a new contact from the given request.
     *
     * @param request the contact data to create
     * @return the saved contact as a response DTO
     */
    @Transactional
    public ContactResponse create(ContactRequest request) {
        logger.info("Creating contact with email: {}", request.email());

        Contact contact = new Contact(
                request.name(),
                request.age(),
                request.email(),
                request.phoneNumber(),
                getCurrentUser()
        );

        Contact saved = contactRepository.save(contact);
        logger.info("Contact created with id: {}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Returns a paginated list of all contacts.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of contact response DTOs
     */
    @Transactional(readOnly = true)
    public Page<ContactResponse> getAll(Pageable pageable) {
        logger.debug("Fetching all contacts with pageable: {}", pageable);
        return contactRepository.findAllByUser(getCurrentUser(), pageable).map(this::toResponse);
    }

    /**
     * Returns a single contact by ID.
     *
     * @param id the contact ID
     * @return the matching contact as a response DTO
     * @throws ResourceNotFoundException if no contact exists with the given ID
     */
    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {
        logger.debug("Fetching contact with id: {}", id);

        Contact contact = contactRepository.findByIdAndUser(id, getCurrentUser())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        return toResponse(contact);
    }

    /**
     * Updates an existing contact with new data.
     *
     * <p>Uses dirty checking — the entity is updated in place within
     * the transaction and saved automatically on commit without
     * an explicit {@code save()} call.</p>
     *
     * @param id             the ID of the contact to update
     * @param updatedContact the new contact data
     * @return the updated contact as a response DTO
     * @throws ResourceNotFoundException if no contact exists with the given ID
     */
    @Transactional
    public ContactResponse update(Long id, ContactRequest updatedContact) {
        logger.info("Updating contact with id: {}", id);

        Contact contact = contactRepository.findByIdAndUser(id, getCurrentUser())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        contact.setName(updatedContact.name());
        contact.setAge(updatedContact.age());
        contact.setEmail(updatedContact.email());
        contact.setPhoneNumber(updatedContact.phoneNumber());

        return toResponse(contact);
    }

    /**
     * Deletes a contact by ID.
     *
     * <p>Existence is checked before deletion to throw a meaningful
     * exception rather than silently doing nothing, which is the
     * default behavior of {@code deleteById()} when ID is not found.</p>
     *
     * @param id the ID of the contact to delete
     * @throws ResourceNotFoundException if no contact exists with the given ID
     */
    @Transactional
    public void delete(Long id) {
        logger.info("Deleting contact with id: {}", id);

        Contact contact = contactRepository.findByIdAndUser(id, getCurrentUser())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found with id: " + id)
                );

        contactRepository.deleteByIdAndUser(id, getCurrentUser());
    }

    /**
     * Searches contacts by name, case-insensitive, with pagination.
     *
     * <p>Returns an empty page immediately if the name is null or blank,
     * avoiding an unnecessary database query.</p>
     *
     * @param name     the name fragment to search for
     * @param pageable pagination and sorting parameters
     * @return a page of matching contact response DTOs, or empty if name is blank
     */
    @Transactional(readOnly = true)
    public Page<ContactResponse> searchByName(String name, Pageable pageable) {
        logger.debug("Searching for contacts with name: {}", name);

        if (name == null || name.isBlank()) {
            return Page.empty(pageable);
        }

        return contactRepository
                .findByNameContainingIgnoreCaseAndUser(name.trim(), getCurrentUser(), pageable)
                .map(this::toResponse);
    }

    /**
     * Converts a {@link Contact} entity to a {@link ContactResponse} DTO.
     * Keeps mapping logic in one place so all methods return a consistent shape.
     *
     * @param contact the entity to convert
     * @return the mapped response DTO
     */
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

    /**
     * Returns the currently authenticated user from the security context.
     * Throws ResourceNotFoundException if the user no longer exists in the database.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));
    }
}
