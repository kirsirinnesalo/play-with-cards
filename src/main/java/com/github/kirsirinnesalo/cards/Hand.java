package com.github.kirsirinnesalo.cards;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Hand {
    private ObservableList<Card> cards;
    private BooleanProperty openHandProperty = new SimpleBooleanProperty();

    public Hand() {
        cards = FXCollections.observableArrayList();
        openHandProperty.set(true);
    }

    public ObservableList<Card> getCards() {
        return cards;
    }

    public int size() {
        return cards.size();
    }

    public void takeCard(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public void forEach(Consumer<? super Card> action) {
        cards.forEach(action);
    }

    public String handAsString() {
        StringBuilder builder = new StringBuilder();
        AtomicReference<String> delimiter = new AtomicReference<>("");
        cards.forEach(card -> {
            builder.append(delimiter.get());
            builder.append(card.toString());
            delimiter.set(", ");
        });
        return builder.toString();
    }

    public void close() {
        openHandProperty.set(false);
    }

    public boolean isOpen() {
        return openHandProperty.get();
    }

    public void open() {
        openHandProperty.set(true);
    }
}
