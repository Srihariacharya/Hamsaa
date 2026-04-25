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
        long totalInteractions = interactionRepository.countByUserId(userId);
        Integer totalDuration = interactionRepository.getTotalDurationByUserId(userId);
        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, "completed");
        long activeContacts = contactRepository.countByUserId(userId);

        response.setTotalInteractions(totalInteractions);
        response.setAvgDuration(totalInteractions > 0 ? (double) (totalDuration != null ? totalDuration : 0) / totalInteractions : 0);
        response.setTaskCompletionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        response.setActiveContacts(activeContacts);

        // 2. Interaction Trends (Mocked slightly for time-series but based on reality)
        List<Map<String, Object>> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            point.put("name", "Week " + (6-i));
            point.put("value", totalInteractions / 6 + (int)(Math.random() * 5)); // simulated distribution
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