package com.contactpro.contactpro.service;

import com.contactpro.contactpro.dto.GoogleContactDto;
import com.contactpro.contactpro.model.Contact;
import com.contactpro.contactpro.model.User;
import com.contactpro.contactpro.repository.ContactRepository;
import com.contactpro.contactpro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GoogleContactSyncService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public GoogleContactSyncService(UserRepository userRepository, ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    public Map<String, Object> syncContacts(Long userId, List<GoogleContactDto> googleContacts) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (GoogleContactDto googleContact : googleContacts) {
            String email = normalize(googleContact.getEmail());
            String phone = normalize(googleContact.getPhone());
            String name = normalize(googleContact.getDisplayName());

            if (isBlank(name) && isBlank(email) && isBlank(phone)) {
                skipped++;
                continue;
            }

            Optional<Contact> existingByEmail = isBlank(email)
                    ? Optional.empty()
                    : contactRepository.findFirstByUserIdAndEmailIgnoreCase(userId, email);

            Optional<Contact> existingByPhone = isBlank(phone)
                    ? Optional.empty()
                    : contactRepository.findFirstByUserIdAndPhone(userId, phone);

            Contact contact = existingByEmail.orElseGet(() -> existingByPhone.orElse(null));

            if (contact == null) {
                Contact newContact = new Contact();
                newContact.setUser(user);
                newContact.setName(defaultIfBlank(name, "Google Contact"));
                newContact.setEmail(email);
                newContact.setPhone(phone);
                newContact.setCategory("google");
                newContact.setNotes("Imported from Google People API");
                contactRepository.save(newContact);
                created++;
                continue;
            }

            boolean changed = false;

            if (isBlank(contact.getName()) && !isBlank(name)) {
                contact.setName(name);
                changed = true;
            }
            if (isBlank(contact.getEmail()) && !isBlank(email)) {
                contact.setEmail(email);
                changed = true;
            }
            if (isBlank(contact.getPhone()) && !isBlank(phone)) {
                contact.setPhone(phone);
                changed = true;
            }
            if (isBlank(contact.getCategory())) {
                contact.setCategory("google");
                changed = true;
            }

            if (changed) {
                contactRepository.save(contact);
                updated++;
            } else {
                skipped++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("fetched", googleContacts.size());
        summary.put("created", created);
        summary.put("updated", updated);
        summary.put("skipped", skipped);

        return summary;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
