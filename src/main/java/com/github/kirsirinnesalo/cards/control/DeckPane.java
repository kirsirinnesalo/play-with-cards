package com.github.kirsirinnesalo.cards.control;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Deck;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class DeckPane extends StackPane {

    private Deck deck;
    private boolean faceUp;
    private int width;
    private int height;

    public DeckPane(Deck deck, boolean faceUp, int width, int height) {
        this.deck = deck;
        this.faceUp = faceUp;
        this.width = width;
        this.height = height;

        setImage();
        setPrefWidth(width);
        setPrefHeight(height);
        setAlignment(Pos.BASELINE_CENTER);
    }

    private void setImage() {
        ObservableList<Node> cards = getChildren();
        if (faceUp) {
            deck.forEach(card -> new CardView(card, width, height));
        } else {
            Card card = new Card(Card.Rank.ACE, Card.Suit.SPADES);
            cards.add(new CardView(card, width, height));
        }
    }

}
