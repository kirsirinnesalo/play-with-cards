package com.github.kirsirinnesalo.cards;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicReference;

public class Player {
    private ObservableList<Card> hand;
    private String name;

    public Player(String name) {
        this.name = name;
        resetHand();
    }

    public String getName() {
        return name;
    }

    public ObservableList<Card> getHand() {
        return hand;
    }

    public void takeCard(Card card) {
        hand.add(card);
    }

    public ObservableList<Card> resetHand() {
        ObservableList<Card> cards = FXCollections.observableArrayList();
        if (null != hand) {
            cards.addAll(hand);
            hand.clear();
        } else {
            hand = FXCollections.observableArrayList();
        }
        return cards;
    }

    public String handAsString() {
        StringBuilder builder = new StringBuilder();
        AtomicReference<String> delimiter = new AtomicReference<>("");
        hand.forEach(card -> {
            builder.append(delimiter.get());
            builder.append(card.toString());
            delimiter.set(", ");
        });
        return builder.toString();
    }

    @Override
    public String toString() {
        return getName() + ": " + handAsString();
    }

}
