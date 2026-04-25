package com.contactpro.contactpro.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * @Entity → tells Hibernate this is a database table
 */
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String category;
    private String notes;
    private String gender;
    private String dob;
    private int followUpFrequency = 30; // default 30 days

    private boolean isBlocked = false;
    private boolean isFavorite = false;

    private LocalDateTime createdAt;

    /*
     * Many contacts belong to ONE user
     * This creates a foreign key column "user_id"
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * Automatically set creation timestamp
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Contact() {}

    // Getters & Setters

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public int getFollowUpFrequency() { return followUpFrequency; }
    public void setFollowUpFrequency(int followUpFrequency) { this.followUpFrequency = followUpFrequency; }
}