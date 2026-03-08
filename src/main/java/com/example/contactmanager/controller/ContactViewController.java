package com.example.contactmanager.controller;

import com.example.contactmanager.dto.ContactRequest;
import com.example.contactmanager.dto.ContactResponse;
import com.example.contactmanager.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// @Controller
// @RequestMapping("/contacts")
public class ContactViewController {

    private final ContactService contactService;

    public ContactViewController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String listContacts(Pageable pageable, Model model) {

        Page<ContactResponse> page = contactService.getAll(pageable);

        model.addAttribute("page", page);
        return "contacts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("contact", new ContactRequest(null, null, null, null));
        return "contacts/form";
    }

    @PostMapping
    public String createContact(
            @Valid @ModelAttribute("contact") ContactRequest request,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "contacts/form";
        }

        contactService.create(request);
        return "redirect:/contacts";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String name,
            Pageable pageable,
            Model model
    ) {

        Page<ContactResponse> page =
                contactService.searchByName(name, pageable);

        model.addAttribute("page", page);
        model.addAttribute("searchTerm", name);

        return "contacts/list";
    }
}
