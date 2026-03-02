package com.example.accessingdatajpa.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ContactRepository extends CrudRepository<com.example.accessingdatajpa.entity.Contact, Long> {

    List<com.example.accessingdatajpa.entity.Contact> findByLastName(String lastName);

    com.example.accessingdatajpa.entity.Contact findById(long id);
}
