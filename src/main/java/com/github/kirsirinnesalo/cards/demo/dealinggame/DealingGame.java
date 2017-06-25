package com.github.kirsirinnesalo.cards.demo.dealinggame;

import com.github.kirsirinnesalo.cards.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

class DealingGame implements Game {
    private Deck deck;

    private int cardsPerHand;
    private List<Player> players;

    DealingGame(int numberOfPlayers, int cardsPerHand) {
        this.cardsPerHand = cardsPerHand;

        deck = new Deck52With2Jokers();

        players = rangeClosed(1, numberOfPlayers)
                .mapToObj(playerNo -> new Player("Player " + playerNo))
                .collect(Collectors.toList());
    }

    void dealHandFor(Player player) {
        rangeClosed(1, cardsPerHand).forEach($ -> player.takeCard(deck.dealCard()));
    }

    boolean enoughCardsInDeckForRound() {
        int cardsLeft = deck.cardsLeft();
        int cardsNeededForRound = players.size() * cardsPerHand;
        return cardsLeft >= cardsNeededForRound;
    }

    String getDeckLabelText() {
        return "Cards in deck: " + deck.cardsLeft();
    }

    List<Player> getPlayers() {
        return players;
    }

    Deck getDeck() {
        return deck;
    }
}
