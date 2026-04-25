package com.contactpro.contactpro.dto;

/*
 * InteractionRequest
 *
 * This DTO represents the data sent by the client
 * when creating a new interaction.
 *
 * Example JSON request:
 *
 * {
 *   "contactId": 3,
 *   "type": "CALL",
 *   "notes": "Discussed project progress",
 *   "duration": 10
 * }
 */

public class InteractionRequest {

    private Long contactId;
    private String type;
    private String notes;
    private Integer duration;
    private String interactionDate; // Optional custom date

    // getters

    public Long getContactId() {
        return contactId;
    }

    public String getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getInteractionDate() {
        return interactionDate;
    }

    // setters

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setInteractionDate(String interactionDate) {
        this.interactionDate = interactionDate;
    }
}