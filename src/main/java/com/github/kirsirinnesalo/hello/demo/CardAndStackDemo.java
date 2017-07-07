package com.github.kirsirinnesalo.hello.demo;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Deck52;
import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.game.GameFX;
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

public class CardAndStackDemo extends GameFX {

    private static final Border EMPTY_STACK_BORDER = new Border(
            new BorderStroke(Color.DIMGRAY, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1))
    );
    private static final Background EMPTY_STACK_BACKGROUND = Utils.getBackgroundWith(Color.LIGHTYELLOW);

    @Override
    protected String getTitle() {
        return "Stacking Cards Demo";
    }

    @Override
    protected Parent createGameTable() {
        Stack stack1 = getStack(200, 100);
        ObservableList<Card> cards = new Deck52().getCards();
        Collections.shuffle(cards);
        IntStream.range(0, 11).forEach(i -> stack1.addCard(getCardView(cards.get(i))));

        Stack stack2 = getStack(500, 100);
        Stack stack3 = getStack(350, 350);

        return new Pane(stack1, getLabelFor(stack1),
                stack2, getLabelFor(stack2),
                stack3, getLabelFor(stack3));
    }

    private Label getLabelFor(Stack stack) {
        Label label = new Label();
        label.setLayoutX(stack.getLayoutX() + 42);
        label.setLayoutY(stack.getLayoutY() - 20);
        label.setAlignment(Pos.BASELINE_CENTER);
        label.textProperty().bind(Bindings.size(stack.getCards()).asString());
        return label;
    }

    private CardView getCardView(Card card) {
        CardView cardView = new CardView(card, 95, 120);
        cardView.turnFaceUp();
        makeDraggable(cardView);
        return cardView;
    }

    private Stack getStack(int x, int y) {
        Stack stack = new Stack(FXCollections.observableArrayList(), 95, 120);
        stack.setLayoutX(x);
        stack.setLayoutY(y);
        stack.setBorder(EMPTY_STACK_BORDER);
        stack.setBackground(EMPTY_STACK_BACKGROUND);
        stack.getCards().addListener(getChangeListenerFor(stack));
        makeDragTarget(stack);
        stack.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                ObservableList<Node> nodes = stack.getParent().getChildrenUnmodifiable();
                nodes.filtered(node -> (node instanceof Stack))
                        .forEach(s -> {
                            if (stack.getCards().size() > 0) {
                                ((Stack) s).addCard(stack.giveCard());
                            }
                        });
            }
        });
        return stack;
    }

    private ListChangeListener<CardView> getChangeListenerFor(Stack stack) {
        return change -> {
            if (change.next()) {
                ObservableList<Node> cardList = stack.getChildren();
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(card -> {
                        Platform.runLater(() -> {
                            if (!cardList.contains(card)) {
                                cardList.add(card);
                                shiftOverlap(stack, card);
                            }
                        });
                    });
                    hideBackground(stack);
                } else if (change.wasRemoved()) {
                    List<? extends CardView> removed = change.getRemoved();
                    Platform.runLater(() -> {
                        List<Node> toBeRemoved = cardList.stream()
                                .filter(removed::contains)
                                .collect(toList());
                        cardList.removeAll(toBeRemoved);
                        if (cardList.size() == 0) {
                            viewEmptyStack(stack);
                        }
                    });
                }
            }
        };
    }

    private void hideBackground(Stack stack) {
        stack.setBorder(null);
        stack.setBackground(Utils.TRANSPARENT_BACKGROUND);
    }

    private void viewEmptyStack(Stack stack) {
        stack.setBorder(EMPTY_STACK_BORDER);
        stack.setBackground(EMPTY_STACK_BACKGROUND);
    }

    private void shiftOverlap(Stack stack, CardView cardView) {
        double shift = 20;
        ObservableList<Node> cardList = stack.getChildren();
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

    private void makeDragTarget(Stack stack) {
        stack.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        stack.setOnDragDropped(event -> {
            CardView card = (CardView) event.getGestureSource();
            ((Stack) card.getParent()).giveCard();
            stack.addCard(card);
        });
    }

    private void makeDraggable(CardView cardView) {
        cardView.setOnDragDetected(event -> {
            ObservableList<CardView> cards = ((Stack) cardView.getParent()).getCards();
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
