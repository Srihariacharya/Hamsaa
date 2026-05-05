package com.contactpro.contactpro.dto;

import java.time.LocalDateTime;

public class ContactResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String category;
    private String gender;
    private String dob;
    private String notes;
    private int followUpFrequency;
    private LocalDateTime lastInteractionDate;
    private boolean isBlocked;
    private boolean isFavorite;
    private LocalDateTime createdAt;

    public ContactResponse(
            Long id,
            String name,
            String phone,
            String email,
            String category,
            String gender,
            String dob,
            String notes,
            int followUpFrequency,
            LocalDateTime lastInteractionDate,
            boolean isBlocked,
            boolean isFavorite,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.category = category;
        this.gender = gender;
        this.dob = dob;
        this.notes = notes;
        this.followUpFrequency = followUpFrequency;
        this.lastInteractionDate = lastInteractionDate;
        this.isBlocked = isBlocked;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCategory() { return category; }
    public String getGender() { return gender; }
    public String getDob() { return dob; }
    public String getNotes() { return notes; }
    public int getFollowUpFrequency() { return followUpFrequency; }
    public LocalDateTime getLastInteractionDate() { return lastInteractionDate; }
    public boolean isBlocked() { return isBlocked; }
    public boolean isFavorite() { return isFavorite; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}