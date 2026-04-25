package com.contactpro.contactpro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String notes;

    private Integer duration;

    private LocalDateTime interactionDate;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    // getters

    public Long getId() { return id; }

    public String getType() { return type; }

    public String getNotes() { return notes; }

    public Integer getDuration() { return duration; }

    public LocalDateTime getInteractionDate() { return interactionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Contact getContact() { return contact; }

    // setters

    public void setType(String type) { this.type = type; }

    public void setNotes(String notes) { this.notes = notes; }

    public void setDuration(Integer duration) { this.duration = duration; }

    public void setInteractionDate(LocalDateTime interactionDate) { this.interactionDate = interactionDate; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setContact(Contact contact) { this.contact = contact; }
}