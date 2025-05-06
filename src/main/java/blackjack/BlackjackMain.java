package blackjack;

import java.util.Scanner;
import java.util.UUID;
import java.util.Random;

public class BlackjackMain {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        BlackjackSession session = new BlackjackSession(UUID.randomUUID(), "localuser", new Deck(), new Random());


        while (true) {
            System.out.println("\n--- NEW GAME ---");
            session.reset();
            
            System.out.println("\nYour current balance: " + session.getBalance() + " units");
            
            while (true) {
                System.out.print("Enter your bet (multiple of 10): ");
                try {
                    int bet = Integer.parseInt(input.nextLine().trim());
                    if (bet % 10 == 0) {
                        session.betAndDeal(bet);
                        break;
                    }
                    System.out.println("");
                } catch (NumberFormatException e) {
                    System.out.println("Not a legal value for betting");
                }
            }

            while (!session.isGameOver()) {
                printState(session);

                if (session.getPhase() == GamePhase.PLAYER_TURN) {
                    System.out.print("Choose action (h)it / (s)tand: ");
                    String command = input.nextLine().trim().toLowerCase();

                    if (command.equals("hit") || command.equals("h")) {
                        session.playerHit();
                    } else if (command.equals("stand") || command.equals("s")) {
                        session.playerStand();
                        session.dealerPlay(); // play dealer immediately
                    } else {
                        System.out.println("Invalid command.");
                    }
                }
            }
            printState(session);
            System.out.println("==> Outcome: " + session.outcome());

            System.out.print("Play again? (yes/no): ");
            String again = input.nextLine().trim().toLowerCase();
            if (!again.equals("yes")) {
                break;
            }
        }

        System.out.println("Thanks for playing!");
        input.close();
    }

    private static void printState(BlackjackSession session) {
        System.out.println("\nYour hand: " + session.getPlayerHand().getCards() +
                " (value: " + session.getPlayerHand().value() + ")");
        
        if (session.getPhase() == GamePhase.RESOLVED) {
            System.out.println("Dealer's hand: " + session.getDealerHand().getCards() +
                    " (value: " + session.getDealerHand().value() + ")");
        } else {
            System.out.println("Dealer shows: " + session.getDealerHand().getCards().get(0));
        }
    }
}

