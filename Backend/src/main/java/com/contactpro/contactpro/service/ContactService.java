package com.contactpro.contactpro.service;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.contactpro.contactpro.repository.ContactRepository;
import com.contactpro.contactpro.repository.InteractionRepository;
import com.contactpro.contactpro.repository.UserRepository;
import com.contactpro.contactpro.model.Contact;
import com.contactpro.contactpro.model.Interaction;
import com.contactpro.contactpro.model.User;
import com.contactpro.contactpro.dto.ContactRequest;
import com.contactpro.contactpro.dto.ContactResponse;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository,
                          InteractionRepository interactionRepository,
                          UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
    }

    /** Normalize a phone number to last 10 digits for comparison */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 10 ? digits.substring(digits.length() - 10) : digits;
    }

    private ContactResponse mapToResponse(Contact contact) {
        LocalDateTime lastDate = interactionRepository.findLastInteractionDate(contact.getId());
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getEmail(),
                contact.getCategory(),
                contact.getGender(),
                contact.getDob(),
                contact.getFollowUpFrequency(),
                lastDate,
                contact.isBlocked(),
                contact.isFavorite(),
                contact.getCreatedAt()
        );
    }

    public Page<ContactResponse> getContactsByUserPaginated(Long userId, int page, int size) {
        Page<Contact> contactPage = contactRepository.findByUserId(userId, PageRequest.of(page, size));
        return contactPage.map(this::mapToResponse);
    }

    public Page<ContactResponse> searchContacts(Long userId, String keyword, int page, int size) {
        Page<Contact> contactPage = contactRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword, PageRequest.of(page, size));
        return contactPage.map(this::mapToResponse);
    }

    public ContactResponse createContact(ContactRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for duplicate by phone
        String normalizedNew = normalizePhone(request.getPhone());
        if (!normalizedNew.isEmpty()) {
            List<Contact> existing = contactRepository.findByUserId(request.getUserId());
            boolean exists = existing.stream()
                .anyMatch(c -> normalizePhone(c.getPhone()).equals(normalizedNew));
            if (exists) {
                return existing.stream()
                    .filter(c -> normalizePhone(c.getPhone()).equals(normalizedNew))
                    .findFirst()
                    .map(this::mapToResponse)
                    .orElseThrow();
            }
        }

        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setCategory(request.getCategory());
        contact.setNotes(request.getNotes());
        contact.setGender(request.getGender() != null ? request.getGender() : com.contactpro.contactpro.util.GenderPredictor.predict(request.getName()));
        contact.setDob(request.getDob());
        contact.setFollowUpFrequency(request.getFollowUpFrequency() > 0 ? request.getFollowUpFrequency() : 30);
        contact.setUser(user);

        Contact saved = contactRepository.save(contact);
        return mapToResponse(saved);
    }

    /**
     * Batch create contacts with DEDUPLICATION by phone number.
     * If a contact with the same phone already exists for this user, it is SKIPPED.
     * This prevents duplicate contacts when the user presses "Sync" multiple times.
     */
    public List<ContactResponse> createContactsBatch(List<ContactRequest> requests) {
        if (requests.isEmpty()) return new ArrayList<>();

        Long userId = requests.get(0).getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Load all existing phone numbers for this user (normalized)
        List<Contact> existingContacts = contactRepository.findByUserId(userId);
        Set<String> existingPhones = existingContacts.stream()
                .map(c -> normalizePhone(c.getPhone()))
                .collect(Collectors.toSet());

        List<Contact> contactsToSave = new ArrayList<>();
        Set<String> phonesInThisBatch = new HashSet<>(); // Prevent duplicates within the same batch

        for (ContactRequest request : requests) {
            String normalizedPhone = normalizePhone(request.getPhone());

            // Skip if phone already in database OR already seen in this batch
            if (!normalizedPhone.isEmpty() &&
                (existingPhones.contains(normalizedPhone) || phonesInThisBatch.contains(normalizedPhone))) {
                continue;
            }

            Contact contact = new Contact();
            contact.setName(request.getName());
            contact.setPhone(request.getPhone());
            contact.setEmail(request.getEmail());
            contact.setCategory(request.getCategory());
            contact.setNotes(request.getNotes());
            contact.setGender(request.getGender() != null ? request.getGender() : com.contactpro.contactpro.util.GenderPredictor.predict(request.getName()));
            contact.setDob(request.getDob());
            contact.setFollowUpFrequency(request.getFollowUpFrequency() > 0 ? request.getFollowUpFrequency() : 30);
            contact.setUser(user);
            contactsToSave.add(contact);

            if (!normalizedPhone.isEmpty()) {
                phonesInThisBatch.add(normalizedPhone);
            }
        }

        List<Contact> savedContacts = contactRepository.saveAll(contactsToSave);
        return savedContacts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Delete duplicate contacts for a user, keeping only the first occurrence
     * of each phone number. Call this once to clean up existing duplicates.
     */
    @Transactional
    public int deleteDuplicateContacts(Long userId) {
        List<Contact> allContacts = contactRepository.findByUserId(userId);
        Set<String> seenPhones = new HashSet<>();
        List<Contact> toDelete = new ArrayList<>();

        for (Contact contact : allContacts) {
            String normalized = normalizePhone(contact.getPhone());
            if (!normalized.isEmpty()) {
                if (seenPhones.contains(normalized)) {
                    // This is a duplicate - delete its interactions first
                    List<Interaction> interactions = interactionRepository.findByContactId(contact.getId());
                    if (!interactions.isEmpty()) {
                        interactionRepository.deleteAll(interactions);
                    }
                    toDelete.add(contact);
                } else {
                    seenPhones.add(normalized);
                }
            }
        }

        contactRepository.deleteAll(toDelete);
        return toDelete.size();
    }

    public ContactResponse updateContact(Long contactId, Long userId, ContactRequest request) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to update this contact");
        }

        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setCategory(request.getCategory());
        contact.setNotes(request.getNotes());
        contact.setGender(request.getGender());
        contact.setDob(request.getDob());
        if (request.getFollowUpFrequency() > 0) {
            contact.setFollowUpFrequency(request.getFollowUpFrequency());
        }

        Contact updated = contactRepository.save(contact);
        return mapToResponse(updated);
    }

    public ContactResponse toggleFavorite(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        if (!contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this contact");
        }
        contact.setFavorite(!contact.isFavorite());
        return mapToResponse(contactRepository.save(contact));
    }

    public ContactResponse toggleBlock(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        if (!contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this contact");
        }
        contact.setBlocked(!contact.isBlocked());
        return mapToResponse(contactRepository.save(contact));
    }

    public void importFromVcf(Long userId, MultipartFile file) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<VCard> vcards = Ezvcard.parse(file.getInputStream()).all();

            for (VCard vcard : vcards) {
                Contact contact = new Contact();
                if (vcard.getFormattedName() != null) {
                    contact.setName(vcard.getFormattedName().getValue());
                }
                if (!vcard.getTelephoneNumbers().isEmpty()) {
                    contact.setPhone(vcard.getTelephoneNumbers().get(0).getText());
                }
                if (!vcard.getEmails().isEmpty()) {
                    contact.setEmail(vcard.getEmails().get(0).getValue());
                }
                contact.setGender(com.contactpro.contactpro.util.GenderPredictor.predict(contact.getName()));
                contact.setUser(user);
                contactRepository.save(contact);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import VCF contacts");
        }
    }

    public List<ContactResponse> getContactsByUser(Long userId) {
        return contactRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ContactResponse getContactById(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        return mapToResponse(contact);
    }

    public Page<ContactResponse> getContactsPaged(Long userId, int page, int size) {
        return contactRepository.findByUserId(userId, PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public List<ContactResponse> searchContacts(String name) {
        return contactRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ContactResponse> getFavoriteContacts(Long userId) {
        return contactRepository.findByUserIdAndIsFavoriteTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ContactResponse> getBlockedContacts(Long userId) {
        return contactRepository.findByUserIdAndIsBlockedTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public String exportToVcf(Long userId) {
        List<Contact> contacts = contactRepository.findByUserId(userId);
        List<VCard> vcards = new ArrayList<>();

        for (Contact contact : contacts) {
            VCard vcard = new VCard();
            vcard.setFormattedName(contact.getName());
            vcard.addTelephoneNumber(contact.getPhone());
            if (contact.getEmail() != null) {
                vcard.addEmail(contact.getEmail());
            }
            vcards.add(vcard);
        }
        return Ezvcard.write(vcards).go();
    }

    public void deleteContact(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this contact");
        }

        List<Interaction> interactions = interactionRepository.findByContactId(contactId);
        if (!interactions.isEmpty()) {
            interactionRepository.deleteAll(interactions);
        }
        contactRepository.delete(contact);
    }

    @Transactional
    public void deleteContactsBatch(List<Long> contactIds, Long userId) {
        for (Long id : contactIds) {
            deleteContact(id, userId);
        }
    }
}