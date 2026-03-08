package com.example.contactmanager.dto;

import java.time.LocalDateTime;

public class ContactResponse {

    private Long id;
    private String name;
    private int age;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public ContactResponse() {} // Required by Swagger

    public ContactResponse(Long id, String name, int age, String email, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}