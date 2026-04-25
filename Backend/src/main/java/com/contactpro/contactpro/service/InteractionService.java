package com.contactpro.contactpro.service;

import com.contactpro.contactpro.dto.InteractionRequest;
import com.contactpro.contactpro.dto.InteractionResponse;
import com.contactpro.contactpro.model.Contact;
import com.contactpro.contactpro.model.Interaction;
import com.contactpro.contactpro.repository.ContactRepository;
import com.contactpro.contactpro.repository.InteractionRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 * InteractionService
 *
 * Responsibility:
 * Handles business logic for interaction tracking.
 *
 * Flow:
 * Controller → Service → Repository → Database
 */

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final ContactRepository contactRepository;

    /*
     * Constructor Injection
     *
     * Spring automatically injects the repositories.
     * This is the recommended dependency injection style.
     */
    public InteractionService(InteractionRepository interactionRepository,
                              ContactRepository contactRepository) {

        this.interactionRepository = interactionRepository;
        this.contactRepository = contactRepository;
    }

    /*
     * Create a new interaction for a contact
     */
    public InteractionResponse createInteraction(InteractionRequest request) {

        // 1️⃣ Validate that the contact exists
        Contact contact = contactRepository.findById(request.getContactId())
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // 2️⃣ Create Interaction entity
        Interaction interaction = new Interaction();
        interaction.setType(request.getType());
        interaction.setNotes(request.getNotes());
        interaction.setDuration(request.getDuration() != null ? request.getDuration().intValue() : 0);
        
        if (request.getInteractionDate() != null) {
            interaction.setInteractionDate(LocalDateTime.parse(request.getInteractionDate()));
        } else {
            interaction.setInteractionDate(LocalDateTime.now());
        }
        
        interaction.setCreatedAt(LocalDateTime.now());
        interaction.setContact(contact);

        // 3️⃣ Save interaction in database
        Interaction saved = interactionRepository.save(interaction);

        // 4️⃣ Convert Entity → DTO
        return new InteractionResponse(
                saved.getId(),
                saved.getType(),
                saved.getNotes(),
                saved.getDuration(),
                saved.getInteractionDate()
        );
    }

    /*
     * Get interaction history of a contact
     */
    public List<InteractionResponse> getInteractionsByContact(Long contactId) {

        List<Interaction> interactions =
                interactionRepository.findByContactId(contactId);

        // Convert Entity list → DTO list
        return interactions.stream()
                .map(i -> new InteractionResponse(
                        i.getId(),
                        i.getType(),
                        i.getNotes(),
                        i.getDuration(),
                        i.getInteractionDate()
                ))
                .collect(Collectors.toList());
    }
}