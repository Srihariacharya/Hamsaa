package com.contactpro.contactpro.controller;

import com.contactpro.contactpro.dto.InteractionRequest;
import com.contactpro.contactpro.dto.InteractionResponse;
import com.contactpro.contactpro.service.InteractionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * InteractionController
 *
 * Responsibility:
 * Exposes REST APIs for interaction management.
 *
 * Flow:
 * Client → Controller → Service → Repository → Database
 */

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;
    private final com.contactpro.contactpro.repository.InteractionRepository interactionRepository;

    public InteractionController(InteractionService interactionService,
                                 com.contactpro.contactpro.repository.InteractionRepository interactionRepository) {
        this.interactionService = interactionService;
        this.interactionRepository = interactionRepository;
    }

    /*
     * API: Create a new interaction
     *
     * POST /api/interactions
     *
     * Request Body Example:
     * {
     *   "contactId": 3,
     *   "type": "CALL",
     *   "notes": "Discussed project requirements",
     *   "duration": 15
     * }
     */
    @PostMapping
    public InteractionResponse createInteraction(
            @RequestBody InteractionRequest request) {

        return interactionService.createInteraction(request);
    }

    @PostMapping("/batch")
    public List<InteractionResponse> createInteractionsBatch(@RequestBody List<InteractionRequest> requests) {
        return interactionService.createInteractionsBatch(requests);
    }

    /*
     * API: Get interaction history of a contact
     *
     * GET /api/interactions/contact/{contactId}
     *
     * Example:
     * GET /api/interactions/contact/3
     */
    @GetMapping("/contact/{contactId}")
    public List<InteractionResponse> getInteractionsByContact(
            @PathVariable Long contactId) {
        return interactionService.getInteractionsByContact(contactId);
    }

    @GetMapping("/user/{userId}")
    public List<InteractionResponse> getInteractionsByUser(@PathVariable Long userId) {
        return interactionService.getInteractionsByUser(userId);
    }

    /**
     * One-time cleanup: deletes all interactions with corrupted duration > 120 min.
     * Call this once after deploying to clean the database.
     * DELETE /api/interactions/cleanup/{userId}
     */
    @DeleteMapping("/cleanup/{userId}")
    public String cleanupCorruptedInteractions(@PathVariable Long userId) {
        interactionRepository.deleteCorruptedInteractions(userId, 120);
        return "Cleanup complete for userId=" + userId;
    }

    /**
     * Hard Reset: deletes ALL interactions for a user.
     * DELETE /api/interactions/reset/{userId}
     */
    @DeleteMapping("/reset/{userId}")
    @org.springframework.transaction.annotation.Transactional
    public String resetInteractions(@PathVariable Long userId) {
        List<com.contactpro.contactpro.model.Interaction> interactions = interactionRepository.findByContactUserId(userId);
        interactionRepository.deleteAll(interactions);
        return "Reset complete. All intelligence logs deleted for userId=" + userId;
    }

    /**
     * Deduplicate: removes duplicate interactions (same contact + same minute).
     * Keeps only the first occurrence of each.
     * DELETE /api/interactions/deduplicate/{userId}
     */
    @DeleteMapping("/deduplicate/{userId}")
    @org.springframework.transaction.annotation.Transactional
    public String deduplicateInteractions(@PathVariable Long userId) {
        List<com.contactpro.contactpro.model.Interaction> all = interactionRepository.findByContactUserId(userId);
        
        // Group by contactId + timestamp rounded to minute
        java.util.Map<String, java.util.List<com.contactpro.contactpro.model.Interaction>> grouped = new java.util.HashMap<>();
        for (com.contactpro.contactpro.model.Interaction i : all) {
            String key = i.getContact().getId() + "_" + 
                (i.getInteractionDate() != null ? i.getInteractionDate().withSecond(0).withNano(0).toString() : "null");
            grouped.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(i);
        }
        
        // For each group with more than 1 entry, delete all but the first
        java.util.List<com.contactpro.contactpro.model.Interaction> toDelete = new java.util.ArrayList<>();
        for (java.util.List<com.contactpro.contactpro.model.Interaction> group : grouped.values()) {
            if (group.size() > 1) {
                // Keep first, delete rest
                for (int idx = 1; idx < group.size(); idx++) {
                    toDelete.add(group.get(idx));
                }
            }
        }
        
        if (!toDelete.isEmpty()) {
            interactionRepository.deleteAll(toDelete);
        }
        
        return "Deduplication complete. Removed " + toDelete.size() + " duplicate interactions for userId=" + userId;
    }
}