package com.contactpro.contactpro.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;

import com.contactpro.contactpro.dto.AnalyticsResponse;
import com.contactpro.contactpro.repository.*;

@Service
public class AnalyticsService {

    private final InteractionRepository interactionRepository;
    private final TaskRepository taskRepository;
    private final ContactRepository contactRepository;

    public AnalyticsService(InteractionRepository interactionRepository,
                            TaskRepository taskRepository,
                            ContactRepository contactRepository) {
        this.interactionRepository = interactionRepository;
        this.taskRepository = taskRepository;
        this.contactRepository = contactRepository;
    }

    public AnalyticsResponse getUserAnalytics(Long userId) {
        AnalyticsResponse response = new AnalyticsResponse();

        // 1. Basic Counts
        long totalInteractions = interactionRepository.countByUserIdAndDurationGreaterThan(userId, 0);
        Integer totalDuration = interactionRepository.getTotalDurationByUserId(userId);
        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, "completed");
        long activeContacts = contactRepository.countByUserId(userId);

        response.setTotalInteractions(totalInteractions);
        response.setAvgDuration(totalDuration != null ? (double) totalDuration / 60.0 : 0.0); // Now represents Total Minutes
        response.setTaskCompletionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        response.setActiveContacts(activeContacts);

        // 2. Interaction Trends (Real grouped data by week)
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<com.contactpro.contactpro.model.Interaction> interactions = interactionRepository.findByContactUserId(userId);
        
        for (int i = 5; i >= 0; i--) {
            LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            long weekDuration = 0; // Total duration in seconds for this week
            for (com.contactpro.contactpro.model.Interaction interaction : interactions) {
                if (interaction.getInteractionDate() != null) {
                    LocalDate interactionDate = interaction.getInteractionDate().toLocalDate();
                    if (!interactionDate.isBefore(weekStart) && !interactionDate.isAfter(weekEnd)) {
                        weekDuration += interaction.getDuration();
                    }
                }
            }
            
            Map<String, Object> point = new HashMap<>();
            point.put("name", "W" + (6-i));
            point.put("value", (int)(weekDuration / 60)); // Show minutes in the chart
            trends.add(point);
        }
        response.setInteractionTrends(trends);

        // 3. Task Distribution
        List<Map<String, Object>> distribution = new ArrayList<>();
        distribution.add(Map.of("name", "Completed", "value", completedTasks));
        distribution.add(Map.of("name", "Pending", "value", taskRepository.countByUserIdAndStatus(userId, "pending")));
        distribution.add(Map.of("name", "In Progress", "value", taskRepository.countByUserIdAndStatus(userId, "inprogress")));
        response.setTaskDistribution(distribution);

        return response;
    }
}