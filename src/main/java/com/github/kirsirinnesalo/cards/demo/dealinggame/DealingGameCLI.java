package com.github.kirsirinnesalo.cards.demo.dealinggame;

import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.cli.Console;

public class DealingGameCLI {

    public static void main(String[] args) {
        new DealingGameCLI(new DealingGame(3, 5)).play();
    }

    private final DealingGame game;

    private DealingGameCLI(DealingGame game) {
        this.game = game;
    }

    private void play() {
        Console.printLine(game.getDeckLabelText());
        Console.printEmptyLine();
        game.getDeck().shuffle();
        int round = 1;
        while (game.enoughCardsInDeckForRound()) {
            Console.printLine("ROUND " + round++ + ":");
            game.getPlayers().forEach(this::dealHandFor);
        }
        Console.printLine("No more cards left for a round.");
    }

    private void dealHandFor(Player player) {
        game.dealHandFor(player);

        Console.printLine("  " + player.getName() + ": " + player.handAsString());
        Console.printLine("  " + game.getDeckLabelText());
        Console.printEmptyLine();
    }
}
