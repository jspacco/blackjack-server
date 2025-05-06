package blackjack;

import java.util.HashMap;
import java.util.Map;

public class Card
{
    public final Rank rank;
    public final Suit suit;

    private Card(Rank rank, Suit suit)
    {
        this.rank = rank;
        this.suit = suit;
    }

    public int value() {
        return rank.value;
    }

    private static final Map<String, Card> CACHE = new HashMap<>();

    public static Card of(Rank rank, Suit suit) 
    {
        String key = rank + "-" + suit;
        CACHE.putIfAbsent(key, new Card(rank, suit));
        return CACHE.get(key);
    }
    
    public boolean isAce() {
        return rank == Rank.ACE;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
