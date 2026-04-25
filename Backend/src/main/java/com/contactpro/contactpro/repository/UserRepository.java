package com.contactpro.contactpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.contactpro.contactpro.model.User;

import java.util.Optional;

/*
 * JpaRepository gives:
 * save()
 * findById()
 * findAll()
 * delete()
 * and many more
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Custom query method.
     * Spring automatically generates SQL.
     */
    Optional<User> findByEmail(String email);
}