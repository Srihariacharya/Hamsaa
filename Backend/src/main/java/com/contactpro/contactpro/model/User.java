package com.contactpro.contactpro.model;

import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * @Entity tells JPA:
 * This class represents a database table.
 */
@Entity

/*
 * @Table lets us define table name explicitly.
 * Without it, table would be named "user"
 * which can conflict with SQL reserved words.
 */
@Table(name = "users")
public class User {

    /*
     * @Id = Primary key
     * @GeneratedValue = Auto increment ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(unique = true)
     * Ensures no two users can register with same email.
     */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /*
     * This will later store BCrypt hashed password.
     */
    @Column(nullable = false)
    private String password;

    private String phone;
    private String company;

    /*
     * Automatically store creation time.
     */
    private LocalDateTime createdAt;

    /*
     * Default constructor required by JPA
     */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Contact> contacts;

    public User() {}

    /*
     * This runs automatically before saving to DB.
     * It sets createdAt timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}