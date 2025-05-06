package blackjack;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSummaryDTO(
    UUID sessionId,
    int balance,
    int currentBet,
    GamePhase phase,
    GameOutcome outcome,
    LocalDateTime lastAccess
) {
    public static SessionSummaryDTO from(UserSessionEntity e) {
        return new SessionSummaryDTO(
            e.getId(),
            e.getBalance(),
            e.getCurrentBet(),
            e.getPhase(),
            e.getOutcome(),
            e.getLastAccess()
        );
    }
}
