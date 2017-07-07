package com.github.kirsirinnesalo.hello.demo.dealinggame;

import com.github.kirsirinnesalo.model.Player;
import com.github.kirsirinnesalo.cli.Console;

public class DealingGameCLI {

    private final DealingGame game;

    private DealingGameCLI(DealingGame game) {
        this.game = game;
    }

    public static void main(String[] args) {
        new DealingGameCLI(new DealingGame(3, 5)).play();
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

        Console.printLine("  " + player.getName() + ": " + player.getHand().handAsString());
        Console.printLine("  " + game.getDeckLabelText());
        Console.printEmptyLine();
    }
}
