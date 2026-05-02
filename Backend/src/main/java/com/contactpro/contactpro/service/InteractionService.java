package com.contactpro.contactpro.service;

import com.contactpro.contactpro.dto.InteractionRequest;
import com.contactpro.contactpro.dto.InteractionResponse;
import com.contactpro.contactpro.model.Contact;
import com.contactpro.contactpro.model.Interaction;
import com.contactpro.contactpro.repository.ContactRepository;
import com.contactpro.contactpro.repository.InteractionRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final ContactRepository contactRepository;

    public InteractionService(InteractionRepository interactionRepository,
                              ContactRepository contactRepository) {
        this.interactionRepository = interactionRepository;
        this.contactRepository = contactRepository;
    }

    /**
     * Robustly parse a date string in multiple formats.
     * Tries each format before falling back to now().
     * This prevents ANY crash from a date format mismatch.
     */
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();

        // Try formats in order of most likely
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        };

        for (String fmt : formats) {
            try {
                if (fmt.equals("yyyy-MM-dd")) {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fmt)).atStartOfDay();
                }
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(fmt));
            } catch (Exception ignored) {}
        }

        // Final fallback — never fail
        return LocalDateTime.now();
    }

    public InteractionResponse createInteraction(InteractionRequest request) {
        Contact contact = contactRepository.findById(request.getContactId())
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        Interaction interaction = new Interaction();
        interaction.setType(request.getType());
        interaction.setNotes(request.getNotes());
        interaction.setDuration(request.getDuration() != null ? request.getDuration().intValue() : 0);
        interaction.setInteractionDate(parseDate(request.getInteractionDate()));
        interaction.setCreatedAt(LocalDateTime.now());
        interaction.setContact(contact);

        Interaction saved = interactionRepository.save(interaction);

        return new InteractionResponse(
                saved.getId(),
                saved.getType(),
                saved.getNotes(),
                saved.getDuration(),
                saved.getInteractionDate()
        );
    }

    public List<InteractionResponse> getInteractionsByContact(Long contactId) {
        return interactionRepository.findByContactId(contactId)
                .stream()
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