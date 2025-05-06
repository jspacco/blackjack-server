package blackjack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck 
{
    private final List<Card> cards = new ArrayList<>();
    private int index = 0;

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(Card.of(rank, suit));
            }
        }
    }

    public Deck(Card... cards) {
        for (Card c : cards) {
            this.cards.add(c);
        }
    }

    public void shuffle(Random random) {
        Collections.shuffle(cards, random);
        index = 0;
    }

    public boolean hasNext() {
        return index < cards.size();
    }

    public Card deal() {
        if (!hasNext()) throw new IllegalStateException("Deck is empty");
        return cards.get(index++);
    }

    public int cardsRemaining() {
        return cards.size() - index;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Card c : cards) {
            b.append(c);
            b.append("\n");
        }
        return b.toString();
    }

    public void partialReshuffle(Random random, Collection<Card> inUse) 
    {
        List<Card> newDeck = new ArrayList<>(52);

        // Step 1: Add in-use cards first (preserve order)
        newDeck.addAll(inUse);

        // Step 2: Add the rest of the cards that are NOT in use
        for (Card c : cards) {
            if (!inUse.contains(c)) {
                newDeck.add(c);
            }
        }

        // Step 3: Shuffle only the part after the in-use cards
        int fixedSize = inUse.size();
        for (int i = fixedSize; i < newDeck.size(); i++) {
            int j = i + random.nextInt(newDeck.size() - i);
            Collections.swap(newDeck, i, j);
        }

        this.cards.clear();
        this.cards.addAll(newDeck);
        this.index = fixedSize;
    }

}
