package com.example.contactmanager.repository;

import com.example.contactmanager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
