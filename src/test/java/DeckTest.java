import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import blackjack.Deck;

public class DeckTest 
{

    @Test
    public void testDeckCreation() 
    {
        // Create a new deck
        Deck deck = new Deck();

        // Check that the deck has 52 cards
        assertEquals(52, deck.cardsRemaining());

        deck.deal();
        deck.deal();
        // Check that the deck has 50 cards after dealing 2
        assertEquals(50, deck.cardsRemaining());

        
    }
    
}
