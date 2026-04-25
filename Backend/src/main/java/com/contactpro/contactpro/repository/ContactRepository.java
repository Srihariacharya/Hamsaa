package com.contactpro.contactpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.contactpro.contactpro.model.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    /*
     * Custom method:
     * Get all contacts of a specific user
     */
    // For normal list
    List<Contact> findByUserId(Long userId);

    // For pagination
    Page<Contact> findByUserId(Long userId, Pageable pageable);

    Page<Contact> findByUserIdAndNameContainingIgnoreCase(
            Long userId,
            String name,
            Pageable pageable
    );

    long countByUserId(Long userId);
// counts total contacts belonging to a specific user

    Page<Contact> findByUserIdAndIsFavoriteTrue(
            Long userId,
            Pageable pageable
    );
    // searches contacts whose name contains the given keyword
    List<Contact> findByNameContainingIgnoreCase(String name);

    List<Contact> findByUserIdAndIsFavoriteTrue(Long userId);
// returns contacts marked as favorite

    List<Contact> findByUserIdAndIsBlockedTrue(Long userId);
// returns contacts marked as blocked

    Optional<Contact> findFirstByUserIdAndEmailIgnoreCase(Long userId, String email);

    Optional<Contact> findFirstByUserIdAndPhone(Long userId, String phone);
}