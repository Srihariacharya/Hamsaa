package com.contactpro.contactpro.repository;

import com.contactpro.contactpro.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findByContactId(Long contactId);

    @Query("""
SELECT i.contact.id, COUNT(i)
FROM Interaction i
GROUP BY i.contact.id
ORDER BY COUNT(i) DESC
""")
    List<Object[]> findMostContactedContacts();

    @Query("""
SELECT MAX(i.interactionDate)
FROM Interaction i
WHERE i.contact.id = :contactId
""")
    LocalDateTime findLastInteractionDate(Long contactId);

    long countByContactId(Long contactId);

    @Query("""
SELECT SUM(i.duration)
FROM Interaction i
WHERE i.contact.id = :contactId
""")
    Integer getTotalDuration(Long contactId);

    @Query("""
SELECT i.contact.id, MAX(i.interactionDate)
FROM Interaction i
GROUP BY i.contact.id
""")
    List<Object[]> findLastInteractionForAllContacts();

    List<Interaction> findByContactUserId(Long userId);

    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.contact.user.id = :userId")
    long countByUserId(Long userId);

    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.contact.user.id = :userId AND i.duration > :duration")
    long countByUserIdAndDurationGreaterThan(Long userId, int duration);

    @Query("SELECT SUM(i.duration) FROM Interaction i WHERE i.contact.user.id = :userId")
    Integer getTotalDurationByUserId(Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Interaction i WHERE i.contact.user.id = :userId AND i.duration > :maxDuration")
    void deleteCorruptedInteractions(Long userId, int maxDuration);

    @Query("SELECT COUNT(DISTINCT i.contact.id) FROM Interaction i WHERE i.contact.user.id = :userId AND i.interactionDate >= :since")
    long countActiveContactsSince(Long userId, LocalDateTime since);

    @Query("SELECT SUM(i.duration) FROM Interaction i WHERE i.contact.user.id = :userId AND i.duration > 0")
    Integer getTotalPickedDurationByUserId(Long userId);
}
