package com.github.kirsirinnesalo.cards;

import javafx.collections.ObservableList;

public class Deck52With2Jokers extends Deck52 {
    @Override
    public ObservableList<Card> setupDeck() {
        ObservableList<Card> cards = super.setupDeck();
        cards.add(new Card(Card.Rank.JOKER, Card.Suit.JOKER, Card.Color.BLACK));
        cards.add(new Card(Card.Rank.JOKER, Card.Suit.JOKER, Card.Color.RED));
        return cards;
    }
}
