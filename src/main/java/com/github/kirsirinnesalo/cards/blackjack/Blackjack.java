package com.github.kirsirinnesalo.cards.blackjack;

public class Blackjack {
    public static void main(String[] args) {
        BlackjackGame game = new BlackjackGame();
        new BlackjackCLI(game).play();
    }
}
