
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import blackjack.BlackjackSession;
import blackjack.Card;
import blackjack.Deck;
import blackjack.GameOutcome;
import blackjack.GamePhase;
import blackjack.GameStateDTO;

import static blackjack.Rank.*;
import static blackjack.Suit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class BlackjackLogicTests {

	UUID sessionId = UUID.fromString("12345678-1234-5678-1234-567812345678");
	Random random = new Random(1);
	String username = "testuser";

	private static final boolean DEBUG = false;
	
	private BlackjackSession createSession(Card... cards) {
		Deck deck = new Deck(cards);
		return new BlackjackSession(sessionId, username, deck, random);
	}

	
	static record PlayerHit(
		int dealerHand,
		int playerHandBefore,
		int playerHandAfter,
		boolean playerBust) 
	{
		PlayerHit(int dealerHand, 
			int playerHandBefore, 
			int playerHandAfter,
			boolean playerBust)
		{
			this.dealerHand = dealerHand;
			this.playerHandBefore = playerHandBefore;
			this.playerHandAfter = playerHandAfter;
			this.playerBust = playerBust;
		}
	}

	private void testSession(BlackjackSession session, GameOutcome expectedOutcome, PlayerHit... hits)
	{
		assertEquals(GamePhase.BETTING, session.getPhase());
		session.betAndDeal(10);
		for (PlayerHit hit : hits) {
			assertEquals(GamePhase.PLAYER_TURN, session.getPhase());
			assertEquals(hit.playerHandBefore, session.getPlayerHand().value());
			assertEquals(hit.dealerHand, session.getDealerHand().value());
			session.playerHit();
			if (hit.playerBust) {
				assertEquals(GamePhase.RESOLVED, session.getPhase());
				assertEquals(GameOutcome.DEALER_WINS, session.outcome());
				return;
			} 
			assertEquals(GamePhase.PLAYER_TURN, session.getPhase());
			assertEquals(hit.playerHandAfter, session.getPlayerValue());
			assertEquals(hit.dealerHand, session.getDealerValue());
		}
		session.playerStand();
		assertEquals(GamePhase.DEALER_TURN, session.getPhase());
		session.dealerPlay();
		assertEquals(GamePhase.RESOLVED, session.getPhase());
		assertEquals(expectedOutcome, session.outcome());
		if (DEBUG) System.out.println(GameStateDTO.from(session));
	}

	@Test
	public void testPlayerBust() {
		BlackjackSession session = createSession(
			Card.of(TEN, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, SPADES),
			Card.of(TWO, SPADES)
		);
		
		testSession(session, GameOutcome.DEALER_WINS,
			new PlayerHit(16, 20, 22, true)
		);
	}


	@Test
	void testAceAceNine() {
		BlackjackSession session = createSession(
			Card.of(ACE, SPADES),
			Card.of(ACE, CLUBS),
			Card.of(TEN, HEARTS),
			Card.of(FOUR, SPADES),
			Card.of(NINE, SPADES),
			Card.of(QUEEN, DIAMONDS)
		);

		testSession(session, GameOutcome.PLAYER_WINS,
			new PlayerHit(14, 12, 21, false)
		);
	}

	

	@Test
	void test21() {
		BlackjackSession session = createSession(
			Card.of(TEN, SPADES), 
			Card.of(FIVE, SPADES),
			Card.of(QUEEN, DIAMONDS),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, SPADES)
		);

		testSession(session, GameOutcome.PLAYER_WINS,
			new PlayerHit(20, 15, 21, false)
		);
	}

	@Test
	public void hitTwiceStillLose()
	{
		BlackjackSession session = createSession(
			Card.of(TEN, SPADES), 
			Card.of(TWO, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(THREE, SPADES),
			Card.of(TWO, SPADES),
			Card.of(TWO, CLUBS),
			Card.of(FIVE, HEARTS)
		);
		
		testSession(session, GameOutcome.DEALER_WINS,
			new PlayerHit(13, 12, 14, false),
			new PlayerHit(13, 14, 16, false)
		);
	}

	@Test
	public void testPlayerBlackjack() {
		BlackjackSession session = createSession(
			Card.of(ACE, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, SPADES)
		);

		session.betAndDeal(10);
		
		assertEquals(GamePhase.RESOLVED, session.getPhase());
		assertEquals(GameOutcome.PLAYER_BLACKJACK, session.outcome());
		assertEquals(15, session.getBalance());
	}

	@Test
	public void testDealerBlackjack() {
		BlackjackSession session = createSession(
			Card.of(FIVE, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(ACE, SPADES)
		);

		session.betAndDeal(10);
		
		assertEquals(GamePhase.RESOLVED, session.getPhase());
		assertEquals(GameOutcome.DEALER_WINS, session.outcome());
		assertEquals(-10, session.getBalance());
	}

	@Test
	public void testBothBlackjack() {
		BlackjackSession session = createSession(
			Card.of(ACE, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(ACE, HEARTS)
		);

		session.betAndDeal(10);
		
		assertEquals(GamePhase.RESOLVED, session.getPhase());
		assertEquals(GameOutcome.PUSH, session.outcome());
		assertEquals(0, session.getBalance());
	}

	@Test
	public void testPlayerStandsAndWins() {
		BlackjackSession session = createSession(
			Card.of(NINE, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, DIAMONDS),
			Card.of(TWO, CLUBS)
		);

		session.betAndDeal(10);
		session.playerStand();
		assertEquals(19, session.getPlayerValue());
		assertEquals(16, session.getDealerValue());
		assertEquals(GamePhase.DEALER_TURN, session.getPhase());
		session.dealerPlay();
		assertEquals(18, session.getDealerValue());
		assertEquals(GamePhase.RESOLVED, session.getPhase());
		assertEquals(GameOutcome.PLAYER_WINS, session.outcome());
	}

	@Test
	public void testHitPush1() {
		BlackjackSession session = createSession(
			Card.of(TWO, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, DIAMONDS),
			Card.of(SEVEN, CLUBS),
			Card.of(THREE, CLUBS)
		);

		testSession(session, GameOutcome.PUSH,
			new PlayerHit(16, 12, 19, false)
		);

	}

	@Test
	public void testHitWin1() {
		BlackjackSession session = createSession(
			Card.of(TWO, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, DIAMONDS),
			Card.of(SEVEN, CLUBS),
			Card.of(TWO, CLUBS)
		);

		testSession(session, GameOutcome.PLAYER_WINS,
			new PlayerHit(16, 12, 19, false)
		);
	}

	@Test
	public void testHitLose1() {
		BlackjackSession session = createSession(
			Card.of(TWO, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(SIX, DIAMONDS),
			Card.of(FIVE, CLUBS),
			Card.of(TWO, CLUBS)
		);

		testSession(session, GameOutcome.DEALER_WINS,
			new PlayerHit(16, 12, 17, false)
		);
	}

	@Test
	public void testHitDealerStays1() {
		BlackjackSession session = createSession(
			Card.of(TWO, SPADES), 
			Card.of(QUEEN, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(KING, DIAMONDS),
			Card.of(FIVE, CLUBS),
			Card.of(TWO, CLUBS)
		);

		testSession(session, GameOutcome.DEALER_WINS,
			new PlayerHit(20, 12, 17, false)
		);
	}

	@Test
	public void testPlayerBustTwoHits() {
		BlackjackSession session = createSession(
			Card.of(TWO, SPADES), 
			Card.of(EIGHT, SPADES),
			Card.of(JACK, DIAMONDS),
			Card.of(KING, DIAMONDS),
			Card.of(FIVE, CLUBS),
			Card.of(SEVEN, HEARTS)
		);

		testSession(session, GameOutcome.DEALER_WINS,
			new PlayerHit(20, 10, 15, false),
			new PlayerHit(20, 15, 22, true)
		);
	}

}
