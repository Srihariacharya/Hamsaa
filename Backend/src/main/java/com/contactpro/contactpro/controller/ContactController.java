package com.contactpro.contactpro.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.contactpro.contactpro.service.ContactService;
import com.contactpro.contactpro.dto.ContactRequest;
import com.contactpro.contactpro.dto.ContactResponse;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /*
     * Create Contact
     */
    @PostMapping
    public ContactResponse createContact(@RequestBody ContactRequest request) {
        return contactService.createContact(request);
    }

    @PostMapping("/batch")
    public List<ContactResponse> createContactsBatch(@RequestBody List<ContactRequest> requests) {
        return contactService.createContactsBatch(requests);
    }

    /*
     * Get contacts of user
     */
    @GetMapping("/user/{userId}")
    public List<ContactResponse> getContactsByUser(@PathVariable Long userId) {
        return contactService.getContactsByUser(userId);
    }

    @GetMapping("/user/{userId}/paginated")
    public Page<ContactResponse> getContactsPaginated(
            @PathVariable Long userId,
            @RequestParam int page,
            @RequestParam int size) {

        return contactService.getContactsByUserPaginated(userId, page, size);
    }

    @GetMapping("/search/{userId}")
    public Page<ContactResponse> searchContacts(
            @PathVariable Long userId,
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size) {

        return contactService.searchContacts(userId, keyword, page, size);
    }

    @GetMapping("/export-vcf")
    public ResponseEntity<String> exportContacts(
            @RequestParam Long userId) {

        String vcf = contactService.exportToVcf(userId);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=contacts.vcf")
                .body(vcf);
    }

    @GetMapping("/search")
    public List<ContactResponse> searchContacts(
            @RequestParam String name) {

        return contactService.searchContacts(name);
    }

    @GetMapping("/favorites")
    public List<ContactResponse> getFavoriteContacts(
            @RequestParam Long userId) {

        return contactService.getFavoriteContacts(userId);
    }
// endpoint to retrieve user's favorite contacts
    @GetMapping("/blocked")
    public List<ContactResponse> getBlockedContacts(
            @RequestParam Long userId) {

        return contactService.getBlockedContacts(userId);
}
// endpoint to retrieve blocked contacts

    @PostMapping("/import-vcf")
    public String importVcfContacts(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {

        contactService.importFromVcf(userId, file);

        return "VCF contacts imported successfully";
    }

    @PutMapping("/{contactId}")
    public ContactResponse updateContact(
            @PathVariable Long contactId,
            @RequestParam Long userId,
            @RequestBody ContactRequest request) {

        return contactService.updateContact(contactId, userId, request);
    }

    @GetMapping("/paged")
    public Page<ContactResponse> getContactsPaged(
            @RequestParam Long userId,
            @RequestParam int page,
            @RequestParam int size) {

        return contactService.getContactsPaged(userId, page, size);
    }

    @PatchMapping("/{contactId}/favorite")
    public ContactResponse toggleFavorite(
            @PathVariable Long contactId,
            @RequestParam Long userId) {

        return contactService.toggleFavorite(contactId, userId);
    }

    @PatchMapping("/{contactId}/block")
    public ContactResponse toggleBlock(
            @PathVariable Long contactId,
            @RequestParam Long userId) {

        return contactService.toggleBlock(contactId, userId);
    }

    @GetMapping("/{contactId}")
    public ContactResponse getContactById(@PathVariable Long contactId) {
        return contactService.getContactById(contactId);
    }

    @DeleteMapping("/{contactId}")
    public String deleteContact(
            @PathVariable Long contactId,
            @RequestParam Long userId) {

        contactService.deleteContact(contactId, userId);
        return "Contact deleted successfully";
    }


}