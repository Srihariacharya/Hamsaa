package com.contactpro.contactpro.service;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setCategory(request.getCategory());
        contact.setNotes(request.getNotes());
        contact.setGender(request.getGender());
        contact.setDob(request.getDob());
        contact.setFollowUpFrequency(request.getFollowUpFrequency() > 0 ? request.getFollowUpFrequency() : 30);
        contact.setUser(user);

        Contact saved = contactRepository.save(contact);
        return mapToResponse(saved);
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
        Contact updated = contactRepository.save(contact);
        return mapToResponse(updated);
    }

    public ContactResponse toggleBlock(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to modify this contact");
        }

        contact.setBlocked(!contact.isBlocked());
        Contact updated = contactRepository.save(contact);
        return mapToResponse(updated);
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
                contact.setUser(user);
                contactRepository.save(contact);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import VCF contacts");
        }
    }

    public List<ContactResponse> getContactsByUser(Long userId) {
        List<Contact> contacts = contactRepository.findByUserId(userId);
        if (contacts.isEmpty()) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                seedDummyContacts(user);
                contacts = contactRepository.findByUserId(userId);
            }
        }
        return contacts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void seedDummyContacts(User user) {
        String[] firstNamesM = {"Arjun", "Rahul", "Vikram", "Aditya", "Rohan", "Karan", "Siddharth", "Amit", "Manish", "Suresh", "Ravi", "Sanjay", "Deepak", "Sunil", "Vijay", "Manoj", "Rajesh", "Anil", "Ajay", "Alok"};
        String[] firstNamesF = {"Priya", "Sneha", "Ananya", "Riya", "Neha", "Pooja", "Aarti", "Kavita", "Swati", "Nidhi", "Divya", "Kriti", "Shweta", "Megha", "Tanvi", "Isha", "Simran", "Jyoti", "Rekha", "Anita"};
        String[] lastNames = {"Sharma", "Verma", "Patel", "Iyer", "Nair", "Singh", "Kumar", "Gupta", "Reddy", "Joshi", "Desai", "Menon", "Agarwal", "Kulkarni", "Mukherjee", "Rao", "Bose", "Chawla", "Malhotra", "Bansal"};
        String[] categories = {"Personal", "Work", "Lead", "Vendor", "Client"};

        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            boolean isMale = random.nextBoolean();
            String firstName = isMale ? firstNamesM[random.nextInt(firstNamesM.length)] : firstNamesF[random.nextInt(firstNamesF.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String gender = isMale ? "Male" : "Female";
            
            long phoneSuffix = 7000000000L + (long)(random.nextDouble() * 2999999999L);
            String phone = "+91 " + phoneSuffix;

            Contact contact = new Contact();
            contact.setName(firstName + " " + lastName);
            contact.setPhone(phone);
            contact.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
            contact.setCategory(categories[random.nextInt(categories.length)]);
            contact.setGender(gender);
            contact.setNotes("Auto-generated dummy contact");
            contact.setFollowUpFrequency(30);
            contact.setUser(user);
            contactRepository.save(contact);
        }
    }

    public ContactResponse getContactById(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        return mapToResponse(contact);
    }

    public Page<ContactResponse> getContactsPaged(Long userId, int page, int size) {
        Page<Contact> contacts = contactRepository.findByUserId(userId, PageRequest.of(page, size));
        return contacts.map(this::mapToResponse);
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
}