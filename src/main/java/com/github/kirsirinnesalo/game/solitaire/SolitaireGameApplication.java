package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.game.FXGameApplication;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Deck52;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.collections.FXCollections;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.kirsirinnesalo.control.Stack.EMPTY_STACK_BACKGROUND;
import static com.github.kirsirinnesalo.control.Stack.EMPTY_STACK_BORDER;

public abstract class SolitaireGameApplication extends FXGameApplication {

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

    private Stack deck;
    private List<Stack> stacks;
    private Stack discardPile;
    private Text gameLostMessage;
    private Text gameWonMessage;
    private Button newGameButton;

    public abstract List<Stack> createStacks();

    public abstract EventHandler<DragEvent> getOnDragDroppedHandler();

    public abstract void deal();

    public abstract void checkIfGameOver();

    public Stack getDeck() {
        return deck;
    }

    public List<Stack> getStacks() {
        return stacks;
    }

    public Stack getDiscardPile() {
        return discardPile;
    }

    public final Card topCardIn(Stack stack) {
        return stack.getCards().stream()
                .reduce((a, b) -> b)
                .orElse(new CardView(new Card(Card.Rank.JOKER, Card.Suit.JOKER)))
                .getCard();
    }

    public final boolean isDoubleClick(MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
    }

    public final void makeDragTarget(Stack stack) {
        stack.setOnDragOver(onDragOverHandler);
        stack.setOnDragDropped(getOnDragDroppedHandler());
    }

    public final void gameWon() {
        gameOver(gameWonMessage);
    }

    public final void gameLost() {
        gameOver(gameLostMessage);
    }

    @Override
    public Parent createGameTable() {
        AnchorPane table = new AnchorPane();
        table.setBackground(Utils.getBackgroundWith(Color.DARKSLATEGRAY));
        table.getChildren().addAll(createDeck(), createStacksGroup(), createDiscardPile(),
                createGameLostMessage(), createGameWonMessage(), createNewGameButton());
        hide(discardPile, gameLostMessage, gameWonMessage, newGameButton);
        enableGame();
        return table;
    }

    public Node createDeck() {
        deck = createStack("deck", 30, 45);
        setUpDeck();
        deck.setOnMouseClicked(e -> deal());
        return deck;
    }

    public void setUpDeck() {
        ObservableList<Card> cards = new Deck52().getCards();
        Collections.shuffle(cards);
        cards.forEach(card -> deck.addCard(createCardView(card)));
        deck.getCards().forEach(CardView::turnFaceDown);
    }

    public Stack createStack(String id, int layoutX, int layoutY) {
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

    private Node createStacksGroup() {
        Group group = new Group();
        stacks = createStacks();
        group.getChildren().addAll(stacks);
        return group;
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

    private Node createDiscardPile() {
        discardPile = createStack("discard", 0, 0);
        return discardPile;
    }

    private CardView createCardView(Card card) {
        CardView cardView = new CardView(card, CARD_WIDTH, CARD_HEIGHT);
        cardView.turnFaceUp();
        makeDraggable(cardView);
        return cardView;
    }

    private void gameOver(Node messageNode) {
        disableGame();
        show(messageNode);
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

    private void show(Node... nodes) {
        setVisible(true, nodes);
    }

    private void hide(Node... nodes) {
        setVisible(false, nodes);
    }

    private void setVisible(boolean visible, Node... nodes) {
        Arrays.stream(nodes).forEach(node -> node.setVisible(visible));
    }

    private void makeDraggable(CardView cardView) {
        cardView.setOnDragDetected(onDragDetectedHandler);
    }

}
