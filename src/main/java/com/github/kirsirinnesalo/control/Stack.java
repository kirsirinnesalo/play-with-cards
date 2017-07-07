package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.cards.control.CardView;

import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

public class Stack extends StackPane {

    private ObservableList<CardView> cards;

    public Stack(ObservableList<CardView> cards) {
        this.cards = cards;
    }

    public Stack(ObservableList<CardView> cards, int width, int height) {
        this(cards);

        setPrefWidth(width);
        setPrefHeight(height);
    }

    public void addCard(CardView card) {
        cards.add(card);
    }

    public CardView giveCard() {
        return cards.remove(cards.size() - 1);
    }

    public ObservableList<CardView> getCards() {
        return cards;
    }

    public void setCards(ObservableList<CardView> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

}
