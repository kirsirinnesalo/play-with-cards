package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.game.GameFX;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;
import com.github.kirsirinnesalo.model.Card.Suit;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AcesUp extends GameFX {

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

        if (targetStack.isEmpty()) {
            sourceStack.giveCard();
            targetStack.addCard(cardView);
        }
    };

    private Stack deck;
    private List<Stack> stacks;
    private Stack discardPile;
    private Text gameLostMessage;
    private Text gameWonMessage;
    private Button newGameButton;

    @Override
    protected String getTitle() {
        return "Aces Up";
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
                createGameLostMessage(), createGameWonMessage(), createNewGameButton());
        hide(gameLostMessage, gameWonMessage, newGameButton);
        return table;
    }

    private Text createGameLostMessage() {
        gameLostMessage = new Text("You lost.");
        messageLayout(gameLostMessage);
        gameLostMessage.setFill(Color.BLACK);
        return gameLostMessage;
    }

    private Text createGameWonMessage() {
        gameWonMessage = new Text("You won!");
        messageLayout(gameWonMessage);
        gameWonMessage.setStyle(
                " -fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);\n" +
                        " -fx-stroke: black;\n" +
                        " -fx-stroke-width: 1;");
        return gameWonMessage;
    }

    private void messageLayout(Text message) {
        message.setLayoutX(100);
        message.setLayoutY(200);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFont(Font.font("Arial Bold", 100));
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

    private void disableGame() {
        deck.setDisable(true);
        deck.setOpacity(0.4);
        stacks.forEach(stack -> {
            stack.setDisable(true);
            stack.setOpacity(0.4);
        });
        show(newGameButton);
    }

    private void enableGame() {
        deck.setDisable(false);
        deck.setOpacity(1);
        stacks.forEach(stack -> {
            stack.setDisable(false);
            stack.setOpacity(1);
        });
        hide(gameWonMessage, gameLostMessage, newGameButton);
    }

    private Node createDeck() {
        deck = createStack("1", 30, 45);
        deck.getCards().addListener(getChangeListenerFor(deck, 1));
        setUpDeck();
        deck.setOnMouseClicked(e -> deal());
        return deck;
    }

    private void setUpDeck() {
        ObservableList<Card> cards = new Deck52().getCards();
        Collections.shuffle(cards);
        cards.forEach(card -> deck.addCard(createCardView(card)));
        deck.getCards().forEach(CardView::turnFaceDown);
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
            stack.getCards().addListener(getChangeListenerFor(stack, 5));
            stack.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = topCardIn(stack);
                    if (existsStackTopGreaterThan(myTopCard)) {
                        discardPile.addCard(stack.giveCard());
                    }
                }
                checkIfGameOver();
            });
        });
        group.getChildren().addAll(stacks);
        return group;
    }

    private void checkIfGameOver() {
        if (deck.isEmpty()) {
            if (stacks.stream().allMatch(this::stackOnlyCardIsAce)) {
                gameOver(gameWonMessage);
            } else if (!movesLeft()) {
                gameOver(gameLostMessage);
            }
        }
    }

    private void gameOver(Node messageNode) {
        disableGame();
        show(messageNode);
    }

    private void show(Node... nodes) {
        setVisible(true, nodes);
    }

    private void hide(Node... nodes) {
        setVisible(false, nodes);
    }

    private void setVisible(boolean visible, Node... nodes) {
        Arrays.stream(nodes).forEach(node -> node.setVisible(visible));
    }

    private boolean movesLeft() {
        return stacks.stream().anyMatch(stack -> existsStackTopGreaterThan(topCardIn(stack)));
    }

    private boolean stackOnlyCardIsAce(Stack stack) {
        List<CardView> cards = stack.getCards();
        return cards.size() == 1 && cards.get(0).getCard().isAce();
    }

    private Node createDiscardPile() {
        discardPile = createStack("1", 0, 0);
        hide(discardPile);
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

    private boolean existsStackTopGreaterThan(Card myTopCard) {
        return stacks.stream()
                .filter(otherStack -> topCardIn(otherStack).suit.equals(myTopCard.suit))
                .anyMatch(otherStack -> {
                    Card otherCard = topCardIn(otherStack);
                    return !myTopCard.isAce() && !otherCard.rank.equals(Rank.JOKER) &&
                            (otherCard.isAce() || otherCard.rank.numericValue() > myTopCard.rank.numericValue());
                });
    }

    private Card topCardIn(Stack stack) {
        return stack.getCards().stream()
                .reduce((a, b) -> b)
                .orElse(new CardView(new Card(Rank.JOKER, Suit.JOKER)))
                .getCard();
    }

    private boolean isDoubleClick(MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
    }

    private ListChangeListener<CardView> getChangeListenerFor(Stack stack, double shift) {
        return change -> {
            if (change.next()) {
                if (change.wasAdded()) {
                    addCardsTo(stack, change.getAddedSubList(), shift);
                } else if (change.wasRemoved()) {
                    removeCardsFrom(stack, change.getRemoved());
                }
            }
        };
    }

    private void removeCardsFrom(Stack stack, List<? extends CardView> cardsToRemove) {
        ObservableList<Node> cardList = stack.getChildren();
        Platform.runLater(() -> {
            List<Node> toBeRemoved = cardList.stream()
                    .filter(cardsToRemove::contains)
                    .collect(toList());
            cardList.removeAll(toBeRemoved);
            if (cardList.size() == 0) {
                viewEmptyBackgroundFor(stack);
            }
        });
    }

    private void addCardsTo(Stack stack, List<? extends CardView> cardsToAdd, double shift) {
        ObservableList<Node> cardList = stack.getChildren();
        cardsToAdd.forEach(card -> {
            Platform.runLater(() -> {
                if (!cardList.contains(card)) {
                    cardList.add(card);
                    shiftOverlap(stack, card, shift);
                }
            });
        });
        hideBackgroundFrom(deck);
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

    private void makeDragTarget(Stack stack) {
        stack.setOnDragOver(onDragOverHandler);
        stack.setOnDragDropped(onDragDroppedHandler);
    }

    private void makeDraggable(CardView cardView) {
        cardView.setOnDragDetected(onDragDetectedHandler);
    }

    private void deal() {
        if (deck.getCards().size() >= stacks.size()) {
            stacks.forEach(stack -> {
                CardView card = deck.giveCard();
                card.turnFaceUp();
                stack.addCard(card);
            });
        }
    }

}
