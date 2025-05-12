package blackjack;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Component
public class SessionManager {
    private final Map<UUID, BlackjackSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, UUID> userToSession = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final UserSessionRepository repository;

    public SessionManager(UserSessionRepository repository) {
        this.repository = repository;
    }

    private BlackjackSession newSession(String username)
    {
        UUID newId = UUID.randomUUID();
        Deck deck = new Deck();
        deck.shuffle(random);
        BlackjackSession session = new BlackjackSession(newId, username, deck, random);

        activeSessions.put(newId, session);
        userToSession.put(username, newId);

        return session;
    }

    public BlackjackSession createNewSession(String username) {
        // if they have a current session, save it and create a new one
        UUID existingId = userToSession.get(username);
        if (existingId != null && activeSessions.containsKey(existingId)) {
            archiveSession(existingId);
        }
        return newSession(username);
    }

    public BlackjackSession createOrResumeSession(String username) {
        UUID existingId = userToSession.get(username);

        if (existingId != null && activeSessions.containsKey(existingId)) {
            return activeSessions.get(existingId);
        }

        return newSession(username);
    }

    public Optional<BlackjackSession> getSession(UUID id) {
        return Optional.ofNullable(activeSessions.get(id));
    }

    public void resetSession(UUID id) {
        getSession(id).ifPresent(BlackjackSession::reset);
    }

    public boolean sessionBelongsTo(UUID id, String username) {
        return getSession(id).map(s -> s.getUsername().equals(username)).orElse(false);
    }

    public Optional<UUID> getActiveSessionIdForUser(String username) {
        return Optional.ofNullable(userToSession.get(username));
    }

    public List<UserSessionEntity> getArchivedSessions(String username) {
        return repository.findByUsernameOrderByLastAccessDesc(username);
    }

    public void archiveSession(UUID sessionId) {
        BlackjackSession session = activeSessions.remove(sessionId);
        if (session != null) {
            userToSession.values().removeIf(id -> id.equals(sessionId));

            UserSessionEntity archived = UserSessionEntity.fromSession(session);
            repository.save(archived);
        }
    }

    public Optional<UserSessionEntity> getArchivedSession(UUID id) {
        return repository.findById(id);
    }
    
    public void resumeSession(BlackjackSession session) {
        UUID id = session.getId();
        activeSessions.put(id, session);
        userToSession.put(session.getUsername(), id);
    }
    

    // Optional: archive all
    
    public void archiveAll() {
        for (UUID id : new ArrayList<>(activeSessions.keySet())) {
            archiveSession(id);
        }
    }
}
