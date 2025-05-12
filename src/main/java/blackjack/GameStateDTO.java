package blackjack;

import java.util.List;
import java.util.UUID;

public record GameStateDTO(
    UUID sessionId,
    List<String> playerCards,
    int playerValue,
    List<String> dealerCards,
    Integer dealerValue,
    String phase,
    String outcome,
    int balance,
    int currentBet,
    boolean canHit,
    boolean canStand,
    boolean gameOver,
    int cardsRemaining,
    boolean reshuffled
) {
    
    public static GameStateDTO from(BlackjackSession s) {
        boolean hideDealer = s.getPhase() == GamePhase.PLAYER_TURN;
        boolean reshuffled = s.consumeReshuffledFlag();

        GameStateDTO dto = new GameStateDTO(
            s.getId(),
            s.getPlayerHand().getCards().stream().map(Card::toString).toList(),
            s.getPlayerHand().value(),
            hideDealer
                ? List.of(s.getDealerHand().getCards().get(0).toString(), "???")
                : s.getDealerHand().getCards().stream().map(Card::toString).toList(),
            hideDealer ? null : s.getDealerHand().value(),
            s.getPhase().toString(),
            s.isGameOver() ? s.outcome().toString() : null,
            s.getBalance(),
            s.getCurrentBet(),
            !s.isGameOver() && s.getPhase() == GamePhase.PLAYER_TURN && s.getPlayerHand().value() < 21,
            !s.isGameOver() && s.getPhase() == GamePhase.PLAYER_TURN,
            s.isGameOver(),
            s.getNumCardsRemaining(),
            reshuffled
        );

        return dto;
    }
}
