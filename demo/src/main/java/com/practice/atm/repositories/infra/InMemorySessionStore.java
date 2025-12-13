package com.practice.atm.repositories.infra;

import com.practice.atm.authentication.AuthSession;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemorySessionStore {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

    public AuthSession CreateSession(String customerId) {

        String sessionId = java.util.UUID.randomUUID().toString();
        AuthSession session = new AuthSession(sessionId,
                customerId,
                Instant.now().plusSeconds(5 * 60));

        sessions.put(sessionId, session);
        return session;
    }

    public Optional<AuthSession> get(String sessionId) {
        AuthSession session = sessions.get(sessionId);
        if (session == null) return Optional.empty();

        if(session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId);
            return Optional.empty();
        }

        return Optional.of(session);
    }
}
