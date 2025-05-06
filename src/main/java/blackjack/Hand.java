package blackjack;

import java.util.LinkedList;
import java.util.List;

public class Hand 
{
    public static final Hand EMPTY = new Hand();
    private List<Card> cards = new LinkedList<>();

    public Hand(Card... dealt) 
    {
        for (Card c : dealt) {
            cards.add(c);
        }
    }

    public boolean isBlackjack() 
    {
        return cards.size() == 2 && value() == 21;
    }

    public boolean isBusted() 
    {
        return value() > 21;
    }

    public void add(Card card) 
    {
        cards.add(card);
    }

    //TODO: double down
    //TODO: split

    public List<Card> getCards() {
        return cards;
    }

    public boolean isSoft() 
    {
        return value() < 21 && cards.stream().filter(c -> c.isAce()).count() > 0;
    }

    public int value()
    {
        int total = 0;
        int numAces = 0;
        for (Card c : cards) {
            if (c.isAce()) numAces++;
            total += c.value();
        }
        while (numAces > 0 && total > 21) {
            total -= 10;
            numAces--;
        }
        return total;
    }
    
}
