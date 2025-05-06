package blackjack;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSessionEntity {

    @Id
    private UUID id;

    private String username;

    private int balance;

    private int currentBet;

    @Enumerated(EnumType.STRING)
    private GamePhase phase;

    @Enumerated(EnumType.STRING)
    private GameOutcome outcome;

    private LocalDateTime lastAccess;

    // Optional fields to help resume
    private int playerValue;
    private int dealerValue;

    public UserSessionEntity() {
        // JPA requires no-arg constructor
    }

    public UserSessionEntity(UUID id, String username) {
        this.id = id;
        this.username = username;
        this.lastAccess = LocalDateTime.now();
        this.balance = 0;
        this.currentBet = 10;
        this.phase = GamePhase.BETTING;
        this.outcome = null;
    }

    public static UserSessionEntity fromSession(BlackjackSession s) {
        UserSessionEntity e = new UserSessionEntity();
        e.setId(s.getId());
        e.setUsername(s.getUsername());
        e.setBalance(s.getBalance());
        e.setCurrentBet(s.getCurrentBet());
        e.setPhase(s.getPhase());
        e.setOutcome(s.outcome());
        e.setLastAccess(LocalDateTime.now());
        return e;
    }
    

    // Getters & Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public GameOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(GameOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }

    public int getPlayerValue() {
        return playerValue;
    }

    public void setPlayerValue(int playerValue) {
        this.playerValue = playerValue;
    }

    public int getDealerValue() {
        return dealerValue;
    }

    public void setDealerValue(int dealerValue) {
        this.dealerValue = dealerValue;
    }
}
