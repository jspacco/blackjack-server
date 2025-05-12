package blackjack;

import java.util.Random;

public class Autoserver 
{
    static int reshuffleCount = 0;
    public static void main(String[] args) throws Exception
    {
        Random random = new Random(6);
        BlackjackSession session = new BlackjackSession(null, "jspacco", new Deck(), random);

        int numGames = 1000;
        int numWins = 0;
        int numLosses = 0;
        int numPushes = 0;
        int numBlackjacks = 0;
        int numDealerBlackjacks = 0;

        for (int i = 0; i < numGames; i++) {
            session.reset();
            checkReshuffle(session, "reset");
            session.betAndDeal(10);
            checkReshuffle(session, "betAndDeal");
            if (session.getPhase() == GamePhase.RESOLVED) {
                
                if (session.isPlayerBlackjack() && session.isDealerBlackjack()) {
                    numBlackjacks++;
                    numDealerBlackjacks++;
                    numPushes++;
                }
                else if (session.isPlayerBlackjack())
                {
                    numWins++;
                    numBlackjacks++;
                }
                else if (session.isDealerBlackjack())
                {
                    numLosses++;
                    numDealerBlackjacks++;
                }
                checkReshuffle(session, "reset after blackjack");
                continue;
            }
            
            // STRATEGY: player turn
            while (!session.isPlayerBusted() && session.getPlayerValue() < 17) {
                session.playerHit();
                checkReshuffle(session, "playerHit");
            }
            if (session.isPlayerBusted()) {
                numLosses++;
                continue;
            }
            
            session.playerStand();
            session.dealerPlay();
            checkReshuffle(session, "dealerPlay");
            if (session.isDealerBusted()) {
                numWins++;
                continue;
            }

            if (session.getPlayerValue() > session.getDealerValue()){
                numWins++;
            } else if (session.getDealerValue() > session.getPlayerValue()) {
                numLosses++;
            } else if (session.getDealerValue() == session.getPlayerValue()) {
                numPushes++;
            } else {
                throw new IllegalStateException("Unexpected game state");
            }
        }

        System.out.println("Games played: " + numGames);
        System.out.println("Wins: " + numWins);
        System.out.println("Losses: " + numLosses);
        System.out.println("Pushes: " + numPushes);
        System.out.println("Blackjacks: " + numBlackjacks);
        System.out.println("Dealer Blackjacks: " + numDealerBlackjacks);
        System.out.println("Balance: " + session.getBalance());
        System.out.println("Reshuffles: " + reshuffleCount);
    }

    private static void checkReshuffle(BlackjackSession session, String msg) {
        if (session.consumeReshuffledFlag()) {
            System.out.println("Reshuffled deck " + msg);
            reshuffleCount++;
        }
    }
    
}
