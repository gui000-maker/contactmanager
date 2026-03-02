package com.example.contactmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ContactmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactmanagerApplication.class, args);
	}

}
