package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.game.solitaire.ShiftDown;
import com.github.kirsirinnesalo.game.solitaire.StackShift;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Stack extends StackPane {

    public static final CornerRadii STACK_CORNER_RADII = new CornerRadii(3);
    public static final Border EMPTY_STACK_BORDER = new Border(
            new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, STACK_CORNER_RADII, new BorderWidths(1))
    );
    public static final Background EMPTY_STACK_BACKGROUND = new Background(
            new BackgroundFill(Color.LIGHTGRAY, STACK_CORNER_RADII, Insets.EMPTY));

    private final BooleanProperty emptyProperty = new SimpleBooleanProperty();

    private ObservableList<CardView> cards;
    private StackShift shift = new ShiftDown(2);

    public Stack(ObservableList<CardView> cards) {
        this.cards = cards;
        emptyProperty.bind(Bindings.size(cards).isEqualTo(0));
        this.cards.addListener(getChangeListener());
    }

    public Stack(ObservableList<CardView> cards, int width, int height) {
        this(cards);
        setPrefWidth(width);
        setPrefHeight(height);
    }

    public void setShift(StackShift shift) {
        this.shift = shift;
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

    private void addCards(List<? extends CardView> cardsToAdd) {
        ObservableList<Node> cardList = getChildren();
        cardsToAdd.forEach(card -> {
            Platform.runLater(() -> {
                if (!cardList.contains(card)) {
                    cardList.add(card);
                    shift.shiftOverlap(this,card);
                }
            });
        });
        hideBackgroundFrom();
    }

    private void removeCards(List<? extends CardView> cardsToRemove) {
        ObservableList<Node> cardList = getChildren();
        Platform.runLater(() -> {
            List<Node> toBeRemoved = cardList.stream()
                    .filter(cardsToRemove::contains)
                    .collect(toList());
            cardList.removeAll(toBeRemoved);
            if (cardList.size() == 0) {
                viewEmptyBackground();
            }
        });
    }

    private void viewEmptyBackground() {
        setBorder(EMPTY_STACK_BORDER);
        setBackground(EMPTY_STACK_BACKGROUND);
    }

    private void hideBackgroundFrom() {
        setBorder(Border.EMPTY);
        setBackground(Background.EMPTY);
    }

    private ListChangeListener<CardView> getChangeListener() {
        return change -> {
            if (change.next()) {
                if (change.wasAdded()) {
                    addCards(change.getAddedSubList());
                } else if (change.wasRemoved()) {
                    removeCards(change.getRemoved());
                }
            }
        };
    }

}
