package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.model.Card;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

import java.util.List;

public class Stack extends StackPane {

    private ObservableList<CardView> cards;
    public final BooleanProperty emptyProperty = new SimpleBooleanProperty();

    public Stack(ObservableList<CardView> cards) {
        this.cards = cards;
        emptyProperty.bind(Bindings.size(cards).isEqualTo(0));
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

    public boolean isEmpty() {
        return emptyProperty.get();
    }

    public void clear() {
        this.cards.clear();
    }

    public void addAll(List<CardView> cards) {
        this.cards.addAll(cards);
    }
}
