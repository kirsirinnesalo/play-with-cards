package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.cli.Console;

import java.util.Scanner;

class BlackjackCLI {
    public static void main(String[] args) {
        BlackjackGame game = new BlackjackGame();
        new BlackjackCLI(game).play();
    }

    private final BlackjackGame game;
    private int round = 0;

    private BlackjackCLI(BlackjackGame board) {
        game = board;
    }

    private void play() {
        do {
            BlackjackPlayer player = game.getPlayer();
            game.getDeck().shuffle();
            game.setBet(doBet(player));
            final Player winner = playGame();
            game.payBet(winner);
            if (player.getMoney() == 0) {
                Console.printLine("You lost all your money.");
                break;
            }
            gameOver();
            game.resetGame();
        } while (playAgain());
        Console.printEmptyLine();
        Console.printLine("You leave with " + game.getPlayer().getMoney() + " €");
    }

    private int doBet(BlackjackPlayer player) {
        int money = player.getMoney();
        int bet;
        do {
            Console.printLine("You have money " + money + " €");
            Console.printLine("How much do you want to bet? ");
            Scanner scanner = new Scanner(System.in);
            bet = scanner.hasNext() ? tryParseInt(scanner.nextLine()) : 0;
        } while (bet <= 0 || bet > money);
        return bet;
    }

    private int tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Player playGame() {
        BlackjackDealer dealer = game.getDealer();
        firstDeal();
        if (!dealer.hasBlackjack()) {
            do {
                dealRound();
            } while (!game.getPlayer().isGameOver());

            game.autoPlay(dealer);
        }
        return game.resolveWinner();
    }

    private void firstDeal() {
        newRound();
        game.deal();
        showHands();
    }

    private void dealRound() {
        newRound();
        if (playerNeedsMoreCards()) {
            game.hit(game.getPlayer());
        } else {
            game.getPlayer().quitGame();
        }
        showHands();
    }

    private boolean playerNeedsMoreCards() {
        if (game.getPlayer().needsMoreCards()) {
            Console.printLine(game.getPlayer().toString());
            Console.printLine("Hit more? [Y/N] ");
            Scanner scanner = new Scanner(System.in);
            return scanner.hasNext() && "y".equalsIgnoreCase(scanner.nextLine());
        }
        return false;
    }

    private void newRound() {
        round++;
        Console.printEmptyLine();
        Console.printLine("ROUND " + round);
    }

    private void showCurrentHand(Player player) {
        Console.printLine(player);
    }

    private void showHands() {
        showCurrentHand(game.getDealer());
        showCurrentHand(game.getPlayer());
    }

    private void showFinalHands() {
        Console.printLine(game.getDealer().toString());
        Console.printLine(game.getPlayer().toString());
    }

    private void gameOver() {
        Console.printEmptyLine();
        Console.printLine("GAME OVER");
        showFinalHands();
        Console.printEmptyLine();
        Console.printLine(game.resolveWinner().getName() + " won.");
        Console.printEmptyLine();
        Console.printLine("You have now " + game.getPlayer().getMoney() + " €");
    }

    private boolean playAgain() {
        Console.printLine("Deal again? [Y/N] ");
        Scanner scanner = new Scanner(System.in);
        return scanner.hasNext() && "y".equalsIgnoreCase(scanner.nextLine());
    }

}
