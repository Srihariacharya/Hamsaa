package com.contactpro.contactpro.dto;

import java.time.LocalDateTime;

/*
 * InteractionResponse
 *
 * This DTO controls what the API returns to the client.
 * We never return the full entity directly.
 *
 * Example API response:
 *
 * {
 *   "id": 5,
 *   "type": "CALL",
 *   "notes": "Discussed project",
 *   "duration": 12,
 *   "interactionDate": "2026-03-04T10:30:00"
 * }
 */

public class InteractionResponse {

    private Long id;
    private String type;
    private String notes;
    private Integer duration;
    private LocalDateTime interactionDate;

    public InteractionResponse(Long id,
                               String type,
                               String notes,
                               Integer duration,
                               LocalDateTime interactionDate) {

        this.id = id;
        this.type = type;
        this.notes = notes;
        this.duration = duration;
        this.interactionDate = interactionDate;
    }

    // getters

    public Long getId() {
        return id;
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

    public LocalDateTime getInteractionDate() {
        return interactionDate;
    }
}