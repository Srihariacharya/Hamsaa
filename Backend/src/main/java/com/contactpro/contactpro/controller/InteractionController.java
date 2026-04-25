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

    /*
     * Constructor Injection
     * Spring automatically injects InteractionService.
     */
    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
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
}