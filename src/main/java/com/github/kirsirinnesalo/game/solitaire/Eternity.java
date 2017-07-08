package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.game.GameFX;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Deck52;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Eternity extends GameFX {

    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 400;
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 120;

    private final EventHandler<DragEvent> onDragOverHandler = event -> {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    };
    private final EventHandler<MouseEvent> onDragDetectedHandler = event -> {
        CardView cardView = (CardView) event.getTarget();
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
    };
    private final EventHandler<DragEvent> onDragDroppedHandler = event -> {
        CardView cardView = (CardView) event.getGestureSource();

        Stack sourceStack = (Stack) cardView.getParent();
        Stack targetStack = (Stack) event.getGestureTarget();

        if (isTargetStackOnLeft(sourceStack, targetStack) && topCardRankEqual(targetStack, cardView)) {
            sourceStack.giveCard();
            targetStack.addCard(cardView);
        }
    };

    private Stack deck;
    private List<Stack> stacks;
    private Stack discardPile;
    private int cardsInGame;
    private Text gameWonMessage;
    private Button newGameButton;

    private boolean topCardRankEqual(Stack targetStack, CardView cardView) {
        return topCardIn(targetStack).rank.equals(cardView.getCard().rank);
    }

    private boolean isTargetStackOnLeft(Stack sourceStack, Stack targetStack) {
        return Integer.valueOf(sourceStack.getId()) > Integer.valueOf(targetStack.getId());
    }

    @Override
    protected String getTitle() {
        return "Ikuisuus / Eternity";
    }

    @Override
    protected double getWidth() {
        return TABLE_WIDTH;
    }

    @Override
    protected double getHeight() {
        return TABLE_HEIGHT;
    }

    @Override
    protected Parent createGameTable() {
        AnchorPane table = new AnchorPane();
        table.setBackground(Utils.getBackgroundWith(Color.DARKSLATEGRAY));
        table.getChildren().addAll(createDeck(), createStacks(), createDiscardPile(),
                createGameWonMessage(), createNewGameButton());
        discardPile.setVisible(false);
        enableGame();
        return table;
    }

    private Button createNewGameButton() {
        newGameButton = new Button("New game");
        newGameButton.setLayoutX(250);
        newGameButton.setLayoutY(300);
        newGameButton.setOnAction(event -> {
            setUpDeck();
            stacks.forEach(Stack::clear);
            discardPile.clear();
            enableGame();
        });
        return newGameButton;
    }

    private void enableGame() {
        deck.setDisable(false);
        deck.setOpacity(1);
        stacks.forEach(stack -> {
            stack.setDisable(false);
            stack.setOpacity(1);
        });
        gameWonMessage.setVisible(false);
        newGameButton.setVisible(false);
    }

    private void setUpDeck() {
        ObservableList<Card> cards = new Deck52().getCards();
        cardsInGame = cards.size();
        Collections.shuffle(cards);
        cards.forEach(card -> deck.addCard(createCardView(card)));
        deck.getCards().forEach(CardView::turnFaceDown);
    }

    private Text createGameWonMessage() {
        gameWonMessage = new Text("You won!");
        gameWonMessage.setLayoutX(100);
        gameWonMessage.setLayoutY(200);
        gameWonMessage.setTextAlignment(TextAlignment.CENTER);
        gameWonMessage.setFont(Font.font("Arial Bold", 100));
        gameWonMessage.setStyle(
                " -fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);\n" +
                        " -fx-stroke: black;\n" +
                        " -fx-stroke-width: 1;");
        return gameWonMessage;
    }

    private Node createDeck() {
        deck = createStack("deck", 30, 45);
        deck.getCards().addListener(getChangeListenerFor(deck, 1));
        setUpDeck();
        deck.setOnMouseClicked(e -> deal());
        return deck;
    }

    private CardView createCardView(Card card) {
        CardView cardView = new CardView(card, CARD_WIDTH, CARD_HEIGHT);
        cardView.turnFaceUp();
        makeDraggable(cardView);
        return cardView;
    }

    private Node createStacks() {
        Group group = new Group();
        stacks = new ArrayList<>(4);
        stacks.addAll(Stream.of(
                createStack("1", 155, 45),
                createStack("2", 255, 45),
                createStack("3", 355, 45),
                createStack("4", 455, 45))
                .collect(toList()));
        stacks.forEach(stack -> {
            makeDragTarget(stack);
            stack.getCards().addListener(getChangeListenerFor(stack, 2));
            stack.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = topCardIn(stack);
                    if (everyStackTopEquals(myTopCard)) {
                        stacks.forEach(s -> discardPile.addCard(s.giveCard()));
                        checkWin();
                    }
                }
            });
        });
        group.getChildren().addAll(stacks);
        return group;
    }

    private void checkWin() {
        if (discardPile.getCards().size() == cardsInGame) {
            gameOver();
        }
    }

    private void gameOver() {
        disableGame();
        gameWonMessage.setVisible(true);
    }

    private void disableGame() {
        deck.setDisable(true);
        deck.setOpacity(0.4);
        stacks.forEach(stack -> {
            stack.setDisable(true);
            stack.setOpacity(0.4);
        });
        newGameButton.setVisible(true);
    }

    private Node createDiscardPile() {
        discardPile = createStack("discard", 580, 45);
        return discardPile;
    }

    private Stack createStack(String id, int layoutX, int layoutY) {
        ObservableList<CardView> cards = FXCollections.observableArrayList();
        Stack stack = new Stack(cards, CARD_WIDTH, CARD_HEIGHT);
        stack.setId(id);
        stack.setLayoutX(layoutX);
        stack.setLayoutY(layoutY);
        stack.setBorder(EMPTY_STACK_BORDER);
        stack.setBackground(EMPTY_STACK_BACKGROUND);
        stack.setCursor(Cursor.HAND);
        return stack;
    }

    private boolean everyStackTopEquals(Card myTopCard) {
        return stacks.stream().allMatch(otherStack -> topCardIn(otherStack).rank.equals(myTopCard.rank));
    }

    private Card topCardIn(Stack stack) {
        return stack.getCards().stream()
                .reduce((a, b) -> b)
                .orElse(new CardView(new Card(Card.Rank.JOKER, Card.Suit.JOKER)))
                .getCard();
    }

    private boolean isDoubleClick(MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
    }

    private void makeDragTarget(Stack stack) {
        stack.setOnDragOver(onDragOverHandler);
        stack.setOnDragDropped(onDragDroppedHandler);
    }

    private void makeDraggable(CardView cardView) {
        cardView.setOnDragDetected(onDragDetectedHandler);
    }

    private ListChangeListener<CardView> getChangeListenerFor(Stack stack, double shift) {
        return change -> {
            if (change.next()) {
                ObservableList<Node> cardList = stack.getChildren();
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(card -> {
                        Platform.runLater(() -> {
                            if (!cardList.contains(card)) {
                                cardList.add(card);
                                shiftOverlap(stack, card, shift);
                            }
                        });
                    });
                    hideBackgroundFrom(stack);
                } else if (change.wasRemoved()) {
                    List<? extends CardView> removed = change.getRemoved();
                    Platform.runLater(() -> {
                        List<Node> toBeRemoved = cardList.stream()
                                .filter(removed::contains)
                                .collect(toList());
                        cardList.removeAll(toBeRemoved);
                        if (cardList.size() == 0) {
                            viewEmptyBackgroundFor(stack);
                        }
                    });
                }
            }
        };
    }

    private void shiftOverlap(Stack stack, CardView cardView, double shift) {
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

    private void hideBackgroundFrom(Stack stack) {
        stack.setBorder(Border.EMPTY);
        stack.setBackground(Background.EMPTY);
    }

    private void viewEmptyBackgroundFor(Stack stack) {
        stack.setBorder(EMPTY_STACK_BORDER);
        stack.setBackground(EMPTY_STACK_BACKGROUND);
    }

    private void deal() {
        if (deck.getCards().size() >= stacks.size()) {
            stacks.forEach(stack -> {
                CardView card = deck.giveCard();
                card.turnFaceUp();
                stack.addCard(card);
            });
        } else if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
            new ArrayDeque<>(stacks)
                    .descendingIterator()
                    .forEachRemaining(stack -> {
                        new ArrayDeque<>(stack.getCards())
                                .descendingIterator()
                                .forEachRemaining(card -> deck.addCard(card));
                        //deck.addAll(stack.getCards());
                        stack.clear();
                    });
            deck.getCards().forEach(CardView::turnFaceDown);
        }
    }

}
