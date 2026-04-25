package com.contactpro.contactpro.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuthStateService {

    private static class StateEntry {
        private final Long userId;
        private final Instant expiresAt;

        private StateEntry(Long userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, StateEntry> states = new ConcurrentHashMap<>();

    public String createState(Long userId) {
        String state = UUID.randomUUID().toString();
        states.put(state, new StateEntry(userId, Instant.now().plusSeconds(600)));
        return state;
    }

    public Long consumeState(String state) {
        StateEntry entry = states.remove(state);
        if (entry == null || Instant.now().isAfter(entry.expiresAt)) {
            throw new IllegalArgumentException("Invalid or expired state");
        }
        return entry.userId;
    }
}
