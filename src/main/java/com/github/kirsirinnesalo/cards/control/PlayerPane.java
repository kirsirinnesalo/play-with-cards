package com.github.kirsirinnesalo.cards.control;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.scene.control.BorderedTitledPane;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class PlayerPane extends StackPane {
    private final Player player;
    private final FlowPane cardPane;
    private final int cardWidth;
    private final int cardHeight;
    private BooleanProperty revealHiddenCard = new SimpleBooleanProperty(true);

    public PlayerPane(Player player, int width, int height, int cardWidth, int cardHeight) {
        this.player = player;
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;

        cardPane = new FlowPane();
        cardPane.setHgap(5);
        cardPane.setVgap(3);
        cardPane.setPadding(new Insets(3));
        cardPane.setPrefHeight(height);
        cardPane.setPrefWidth(width);

        player.getHand().addListener(this::updatePlayerBoxView);

        getChildren().add(new BorderedTitledPane(player.getName(), cardPane));
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
                cardPane.getChildren().add(cardView);
            } else if (change.wasRemoved()) {
                refreshPlayerHand();
            }
        }
    }

    private void refreshPlayerHand() {
        ObservableList<Node> cards = cardPane.getChildren();
        cards.clear();
        player.getHand().forEach(card -> cards.add(new CardView(card, cardWidth, cardHeight)));
    }

}
