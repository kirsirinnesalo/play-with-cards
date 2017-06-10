package com.github.kirsirinnesalo.cards;

import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Deck {

    private ObservableList<Card> cards;

    Deck() {
        cards = setupDeck();
    }

    abstract ObservableList<Card> setupDeck();

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        return cards.remove(getLastIndexOfStack());
    }

    private int getLastIndexOfStack() {
        return cards.size() - 1;
    }

    public int cardsLeft() {
        return cards.size();
    }

    public void forEach(Consumer<? super Card> action) {
        cards.forEach(action);
    }

    @Override
    public String toString() {
        return cards.stream().map(Card::toString).collect(Collectors.joining(","));
    }

    public String getLabel() {
        return "Cards in deck: " + cardsLeft();
    }

    public void addAll(ObservableList<Card> cards) {
        this.cards.addAll(cards);
    }
}
