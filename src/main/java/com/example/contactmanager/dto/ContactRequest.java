package com.example.contactmanager.dto;

import jakarta.validation.constraints.*;

public class ContactRequest {

        @NotBlank(message = "Name cannot be empty")
        @Size(max = 50, message = "Name must be at most 50 characters")
        private String name;

        @Min(value = 0, message = "Age must be >= 0")
        @Max(value = 120, message = "Age must be <= 120")
        private Integer age;

        @Email(message = "Email must be valid")
        private String email;

        @Pattern(regexp = "\\+?[0-9\\-]{7,15}", message = "Invalid phone number")
        private String phoneNumber;

        public ContactRequest() {} // Required by Swagger

        public ContactRequest(String name, Integer age, String email, String phoneNumber) {
                this.name = name;
                this.age = age;
                this.email = email;
                this.phoneNumber = phoneNumber;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}