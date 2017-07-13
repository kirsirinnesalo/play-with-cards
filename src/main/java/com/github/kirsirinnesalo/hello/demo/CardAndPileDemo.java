package com.github.kirsirinnesalo.hello.demo;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Deck52;
import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Pile;
import com.github.kirsirinnesalo.game.FXGameApplication;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CardAndPileDemo extends FXGameApplication {

    private static final Border EMPTY_PILE_BORDER = new Border(
            new BorderStroke(Color.DIMGRAY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1))
    );
    private static final Background EMPTY_PILE_BACKGROUND = Utils.getBackgroundWith(Color.LIGHTYELLOW);

    @Override
    public String getTitle() {
        return "Cards and Piles Demo";
    }

    @Override
    public Parent createGameTable() {
        Pile pile1 = getPile(200, 100);
        ObservableList<Card> cards = new Deck52().getCards();
        Collections.shuffle(cards);
        IntStream.range(0, 11).forEach(i -> pile1.addCard(getCardView(cards.get(i))));

        Pile pile2 = getPile(500, 100);
        Pile pile3 = getPile(350, 350);

        return new Pane(pile1, getLabelFor(pile1),
                pile2, getLabelFor(pile2),
                pile3, getLabelFor(pile3));
    }

    private Label getLabelFor(Pile pile) {
        Label label = new Label();
        label.setLayoutX(pile.getLayoutX() + 42);
        label.setLayoutY(pile.getLayoutY() - 20);
        label.setAlignment(Pos.BASELINE_CENTER);
        label.textProperty().bind(Bindings.size(pile.getCards()).asString());
        return label;
    }

    private CardView getCardView(Card card) {
        CardView cardView = new CardView(card, 95, 120);
        cardView.turnFaceUp();
        makeDraggable(cardView);
        return cardView;
    }

    private Pile getPile(int x, int y) {
        Pile pile = new Pile(FXCollections.observableArrayList(), 95, 120);
        pile.setLayoutX(x);
        pile.setLayoutY(y);
        pile.setBorder(EMPTY_PILE_BORDER);
        pile.setBackground(EMPTY_PILE_BACKGROUND);
        pile.getCards().addListener(getChangeListenerFor(pile));
        makeDragTarget(pile);
        pile.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                ObservableList<Node> nodes = pile.getParent().getChildrenUnmodifiable();
                nodes.filtered(node -> (node instanceof Pile))
                        .forEach(s -> {
                            if (pile.getCards().size() > 0) {
                                ((Pile) s).addCard(pile.giveCard());
                            }
                        });
            }
        });
        return pile;
    }

    private ListChangeListener<CardView> getChangeListenerFor(Pile pile) {
        return change -> {
            if (change.next()) {
                ObservableList<Node> cardList = pile.getChildren();
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(card -> {
                        Platform.runLater(() -> {
                            if (!cardList.contains(card)) {
                                cardList.add(card);
                                shiftOverlap(pile, card);
                            }
                        });
                    });
                    hideBackground(pile);
                } else if (change.wasRemoved()) {
                    List<? extends CardView> removed = change.getRemoved();
                    Platform.runLater(() -> {
                        List<Node> toBeRemoved = cardList.stream()
                                .filter(removed::contains)
                                .collect(toList());
                        cardList.removeAll(toBeRemoved);
                        if (cardList.size() == 0) {
                            viewEmptyPile(pile);
                        }
                    });
                }
            }
        };
    }

    private void hideBackground(Pile pile) {
        pile.setBorder(null);
        pile.setBackground(Utils.TRANSPARENT_BACKGROUND);
    }

    private void viewEmptyPile(Pile pile) {
        pile.setBorder(EMPTY_PILE_BORDER);
        pile.setBackground(EMPTY_PILE_BACKGROUND);
    }

    private void shiftOverlap(Pile pile, CardView cardView) {
        double shift = 20;
        ObservableList<Node> cardList = pile.getChildren();
        int cardCount = cardList.size();
        if (cardCount > 0) {
            if (cardCount > 1) {
                double translateY = (cardCount - 2) * shift;
                cardView.setTranslateY(translateY + shift);
            } else {
                cardView.setTranslateY(0);
            }
        }
    }

    private void makeDragTarget(Pile pile) {
        pile.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        pile.setOnDragDropped(event -> {
            CardView card = (CardView) event.getGestureSource();
            ((Pile) card.getParent()).giveCard();
            pile.addCard(card);
        });
    }

    private void makeDraggable(CardView cardView) {
        cardView.setOnDragDetected(event -> {
            ObservableList<CardView> cards = ((Pile) cardView.getParent()).getCards();
            if (!cards.get(cards.size() - 1).equals(cardView)) {
                event.setDragDetect(false);
            } else {
                CardView card = (CardView) event.getSource();

                ClipboardContent content = new ClipboardContent();
                content.putString(card.toString());

                Dragboard dragboard = card.startDragAndDrop(TransferMode.MOVE);
                dragboard.setContent(content);

                ImageView cardSnapshot = new ImageView(card.snapshot(null, null));
                dragboard.setDragView(cardSnapshot.getImage(), event.getX(), event.getY());
            }
            event.consume();
        });
    }

}
