package blackjack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BlackjackSession {
    private final UUID id;
    private final String username;
    private final Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    private GamePhase phase;
    private GameOutcome outcome = null;
    private Random random;
    private int balance = 0;
    private int currentBet = 10;
    private boolean reshuffled = false;


    public BlackjackSession(UUID id, String username, Deck deck, Random random) 
    {
        this.id = id;
        this.username = username;
        this.deck = deck;
        this.random = random;
        reset();
    }

    public Hand getPlayerHand()
    {
        if (this.playerHand == null) return Hand.EMPTY;
        return this.playerHand;
    }

    public Hand getDealerHand()
    {
        if (this.dealerHand == null) return Hand.EMPTY;
        return this.dealerHand;
    }

    public void reset() {
        this.phase = GamePhase.BETTING;
        this.outcome = null;
        if (deck.cardsRemaining() < 4) {
            deck.shuffle(random);
            reshuffled = true;
        }
        this.playerHand = null;
        this.dealerHand = null;
    }

    public void betAndDeal(int units) {
        if (phase != GamePhase.BETTING) throw new IllegalStateException("Cannot bet in game phase "+phase);
        if (units <= 0 || units % 10 != 0 || units > 1000) throw new IllegalArgumentException("Bet must be a positive multiple of 10 between 10 and 1,000");
        this.currentBet = units;

        this.playerHand = new Hand(deck.deal(), deck.deal());
        this.dealerHand = new Hand(deck.deal(), deck.deal());
        phase = GamePhase.PLAYER_TURN;

        if (this.dealerHand.isBlackjack() && this.playerHand.isBlackjack()) {
            push();
        } else if (this.dealerHand.isBlackjack()) {
            dealerWins();
        } else if (this.playerHand.isBlackjack()) {
            playerBlackjack();
        } 
    }

    public UUID getId() {
        return id;
    }

    private Card dealOne()
    {
        if (!deck.hasNext()) {
            partialShuffle();
        }
        return deck.deal();
    }

    public void playerHit() 
    { 
        if (phase != GamePhase.PLAYER_TURN){
            throw new IllegalStateException("Can not hit during phase " + phase);
        }
        Card card = dealOne();
        playerHand.add(card);
        if (isPlayerBusted()) {
            dealerWins();
        }
    }

    private void partialShuffle()
    {
        // reshuffle only the cards not in use
        reshuffled = true;
        Set<Card> inUse = new HashSet<Card>(playerHand.getCards());
        if (inUse.size() >= 50) {
            throw new IllegalStateException("WTF, >= 50 cards are in use!");
        }
        inUse.addAll(dealerHand.getCards());
        deck.partialReshuffle(random, inUse);
    }

    public void playerStand()
    {
        if (phase != GamePhase.PLAYER_TURN) {
            throw new IllegalStateException(phase + " is not the player's turn");
        }
        phase = GamePhase.DEALER_TURN;
    }

    public void dealerPlay()
    {
        if (phase != GamePhase.DEALER_TURN) {
            throw new IllegalStateException("Dealer cannot play during phase " + phase);
        }
        // hit below 17
        while (getDealerValue() < 17) {
            dealerHand.add(dealOne());
        }
        // check who wins
        if (isDealerBusted() || getPlayerValue() > getDealerValue())
        {
            playerWins();
        }
        else if (getDealerValue() > getPlayerValue())
        {
            dealerWins();
        }
        else
        {
            push();
        }
    }

    private void playerBlackjack() {
        balance += currentBet + currentBet / 2;
        outcome = GameOutcome.PLAYER_BLACKJACK;
        phase = GamePhase.RESOLVED;
    }

    private void playerWins() {
        balance += currentBet;
        outcome = GameOutcome.PLAYER_WINS;
        phase = GamePhase.RESOLVED;
    }

    private void dealerWins() {
        balance -= currentBet;
        outcome = GameOutcome.DEALER_WINS;
        phase = GamePhase.RESOLVED;
    }

    private void push() {
        outcome = GameOutcome.PUSH;
        phase = GamePhase.RESOLVED;
    }

    public static BlackjackSession fromEntity(UserSessionEntity entity) {
        Deck deck = new Deck(); // or something smarter
        deck.shuffle(new Random()); // reset deck or restore if you stored it
    
        BlackjackSession session = new BlackjackSession(entity.getId(), entity.getUsername(), deck, new Random());
        session.setBalance(entity.getBalance());
        session.setCurrentBet(entity.getCurrentBet());
        session.setPhase(entity.getPhase());
        session.setOutcome(entity.getOutcome());
        return session;
    }
    

    public GameOutcome outcome()
    {
        return outcome;
    }

    public boolean isGameOver() {
        return phase == GamePhase.RESOLVED;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public int getBalance() {
        return balance;
    }
    
    public int getCurrentBet() {
        return currentBet;
    }

    public String getUsername() {
        return username;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }
    public void setOutcome(GameOutcome outcome) {
        this.outcome = outcome;
    }

	public boolean consumeReshuffledFlag() {
		boolean result = reshuffled;
        reshuffled = false;
        return result;
	}

    public int getPlayerValue() {
        return playerHand.value();
    }

    public int getDealerValue() {
        return dealerHand.value();
    }

    public boolean isPlayerBusted() {
        return playerHand.isBusted();
    }

    public boolean isDealerBusted() {
        return dealerHand.isBusted();
    }

    public int getNumCardsRemaining() {
        return deck.cardsRemaining();
    }
}

