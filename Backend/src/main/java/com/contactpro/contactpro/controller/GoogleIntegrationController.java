package com.contactpro.contactpro.controller;

import com.contactpro.contactpro.dto.GoogleContactDto;
import com.contactpro.contactpro.service.GoogleContactSyncService;
import com.contactpro.contactpro.service.GoogleOAuthService;
import com.contactpro.contactpro.service.GooglePeopleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/google")
public class GoogleIntegrationController {

    private final GoogleOAuthService googleOAuthService;
    private final GooglePeopleService googlePeopleService;
    private final GoogleContactSyncService googleContactSyncService;

    public GoogleIntegrationController(
            GoogleOAuthService googleOAuthService,
            GooglePeopleService googlePeopleService,
            GoogleContactSyncService googleContactSyncService
    ) {
        this.googleOAuthService = googleOAuthService;
        this.googlePeopleService = googlePeopleService;
        this.googleContactSyncService = googleContactSyncService;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> authUrl(@RequestParam Long userId) {
        String url = googleOAuthService.buildAuthUrl(userId);
        return ResponseEntity.ok(Map.of("authUrl", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam String state) throws Exception {
        googleOAuthService.handleCallback(code, state);
        return ResponseEntity.ok("Google account connected successfully. You can close this tab.");
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<GoogleContactDto>> contacts(@RequestParam Long userId) throws Exception {
        String accessToken = googleOAuthService.getValidAccessToken(userId);
        List<GoogleContactDto> contacts = googlePeopleService.getMyContacts(accessToken);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync(@RequestParam Long userId) throws Exception {
        String accessToken = googleOAuthService.getValidAccessToken(userId);
        List<GoogleContactDto> contacts = googlePeopleService.getMyContacts(accessToken);
        Map<String, Object> summary = googleContactSyncService.syncContacts(userId, contacts);
        return ResponseEntity.ok(summary);
    }
}
