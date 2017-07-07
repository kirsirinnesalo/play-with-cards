package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Hand;
import com.github.kirsirinnesalo.scene.control.BorderedTitledPane;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class HandPane extends StackPane {
    private final Pane cardPane;
    private final int cardWidth;
    private final int cardHeight;
    private BooleanProperty revealHiddenCard = new SimpleBooleanProperty(true);
    private double translateX;

    public HandPane(String title, Hand hand, int cardWidth, int cardHeight) {
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.translateX = cardWidth * 0.2;

        cardPane = new StackPane();
        cardPane.setPadding(new Insets(5));
        cardPane.setPrefHeight(cardHeight + 50);

        hand.getCards().addListener(this::updatePlayerBoxView);

        getChildren().add(new BorderedTitledPane(title, cardPane));
    }

    public Pane getCardPane() {
        return cardPane;
    }

    public void hideHiddenCard() {
        revealHiddenCard.setValue(false);
    }

    public void revealHiddenCard() {
        revealHiddenCard.setValue(true);
    }

    public BooleanProperty getRevealHiddenCardProperty() {
        return revealHiddenCard;
    }

    private void updatePlayerBoxView(ListChangeListener.Change<? extends Card> change) {
        if (change.next()) {
            if (change.wasAdded()) {
                Card card = change.getAddedSubList().get(0);
                CardView cardView = new CardView(card, cardWidth, cardHeight);
                cardView.turnFaceUp();
                shiftOverlap(cardView);
                StackPane.setAlignment(cardView, Pos.BOTTOM_LEFT);
            } else if (change.wasRemoved()) {
                removeCardViews(change.getRemoved());
            }
        }
    }

    private void shiftOverlap(CardView cardView) {
        ObservableList<Node> cardList = cardPane.getChildren();
        int cardCount = cardList.size();
        if (cardCount > 0) {
            double shift = 0;
            if (cardCount > 1) {
                shift = (cardCount - 1) * translateX;
            }
            cardView.setTranslateX(translateX + shift);
        }
        cardList.add(cardView);
    }

    private void removeCardViews(List<? extends Card> removedCards) {
        ObservableList<Node> cardList = cardPane.getChildren();
        List<CardView> viewsToBeRemoved = new ArrayList<>();
        removedCards.forEach(card -> cardList.forEach(node -> {
            CardView cardView = (CardView) node;
            if (cardView.getCard().equals(card)) {
                viewsToBeRemoved.add(cardView);
            }
        }));
        cardList.removeAll(viewsToBeRemoved);
    }
}
