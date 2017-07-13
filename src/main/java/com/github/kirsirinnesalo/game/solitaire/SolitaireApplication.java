package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Pile;
import com.github.kirsirinnesalo.game.FXGameApplication;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Deck52;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.stream.Stream;

import static com.github.kirsirinnesalo.control.Pile.EMPTY_PILE_BACKGROUND;
import static com.github.kirsirinnesalo.control.Pile.EMPTY_PILE_BORDER;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;

public abstract class SolitaireApplication extends FXGameApplication {

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
    };

    private Pile stock;
    private List<Pile> tableauPiles;
    private List<Pile> foundationPiles;
    private List<Pile> reservePiles;
    private Pile discardPile;
    private Group gameOverNode;

    public abstract List<Pile> createTableauPiles();

    public abstract List<Pile> createFoundationPiles();

    public abstract List<Pile> createReservePiles();

    public abstract void deal();

    public abstract void hit();

    public abstract EventHandler<DragEvent> getOnDragDroppedHandler();

    public Pile getStock() {
        return stock;
    }

    public List<Pile> getTableauPiles() {
        return tableauPiles;
    }

    public List<Pile> getFoundationPiles() {
        return foundationPiles;
    }

    public List<Pile> getReservePiles() {
        return reservePiles;
    }

    public Pile getDiscardPile() {
        return discardPile;
    }

    public final boolean isDoubleClick(MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
    }

    public final void makeDragTarget(Pile pile) {
        pile.setOnDragOver(onDragOverHandler);
        pile.setOnDragDropped(getOnDragDroppedHandler());
    }

    public void gameWon() {
        getGameOverMessage().ifPresent(this::setGameWonMessageTo);
        gameOver();
    }

    public void gameLost() {
        getGameOverMessage().ifPresent(this::setGameLostMessageTo);
        gameOver();
    }

    @Override
    public Parent createGameTable() {
        AnchorPane table = new AnchorPane();
        table.setBackground(Utils.getBackgroundWith(Color.DARKSLATEGRAY));
        table.getChildren().addAll(createStock(), createPiles(), createGameOverNode());
        hide(discardPile, gameOverNode);
        enableGame();
        deal();
        return table;
    }

    public Node createStock() {
        stock = createPile("stock", 30, 45);
        setUpStock();
        stock.setOnMouseClicked(e -> hit());
        return stock;
    }

    public void setUpStock() {
        ObservableList<Card> cards = new Deck52().getCards();
        shuffle(cards);
        cards.forEach(card -> stock.addCard(createCardView(card)));
        stock.getCards().forEach(card -> {
            card.turnFaceDown();
            card.setTranslateX(0);
        });
    }

    public Pile createPile(String id, int layoutX, int layoutY) {
        ObservableList<CardView> cards = FXCollections.observableArrayList();
        Pile pile = new Pile(cards, CARD_WIDTH, CARD_HEIGHT);
        pile.setId(id);
        pile.setLayoutX(layoutX);
        pile.setLayoutY(layoutY);
        pile.setBorder(EMPTY_PILE_BORDER);
        pile.setBackground(EMPTY_PILE_BACKGROUND);
        pile.setCursor(Cursor.HAND);
        return pile;
    }

    private Node createGameOverNode() {
        gameOverNode = new Group();
        gameOverNode.getChildren().addAll(createGameOverMessage(), createGameOverButtons());
        return gameOverNode;
    }

    private Node createGameOverButtons() {
        HBox box = new HBox();
        box.getChildren().addAll(createNewGameButton(), createQuitGameButton());
        box.setSpacing(100);
        box.setAlignment(Pos.BASELINE_CENTER);
        box.setPrefWidth(getWidth());
        box.setLayoutY(300);
        return box;
    }

    private Text createGameOverMessage() {
        Text message = new Text();
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFont(Font.font("Arial Bold", 100));
        message.setLayoutY(200);
        return message;
    }

    private Node createPiles() {
        tableauPiles = createTableauPiles();
        foundationPiles = createFoundationPiles();
        reservePiles = createReservePiles();
        discardPile = createDiscardPile();

        Group group = new Group();
        ObservableList<Node> groupChildren = group.getChildren();
        Stream.of(tableauPiles, foundationPiles, reservePiles)
                .filter(Objects::nonNull)
                .forEach(groupChildren::addAll);
        if (Optional.ofNullable(discardPile).isPresent()) {
            groupChildren.add(discardPile);
        }
        return group;
    }

    private Optional<Text> getGameOverMessage() {
        return gameOverNode.getChildren().stream()
                .filter(Text.class::isInstance).map(Text.class::cast)
                .findFirst();
    }

    private void setGameLostMessageTo(Text text) {
        text.setText("Hävisit.");
        text.setFill(Color.BLACK);
        text.setStyle(null);
        text.setLayoutX(getWidth() / 2 - text.getLayoutBounds().getWidth() / 2);

    }

    private void setGameWonMessageTo(Text text) {
        text.setText("Sinä voitit!");
        text.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);\n" +
                "-fx-stroke: black;\n" +
                "-fx-stroke-width: 1;");
        text.setLayoutX(getWidth() / 2 - text.getLayoutBounds().getWidth() / 2);
    }

    private Button createNewGameButton() {
        Button newGameButton = new Button("Uusi peli");
        newGameButton.setOnAction(event -> {
            getAllPiles().forEach(Pile::clear);
            setUpStock();
            enableGame();
            deal();
        });
        return newGameButton;
    }

    private Button createQuitGameButton() {
        Button quitGameButton = new Button("Lopeta");
        quitGameButton.setOnAction(e -> Platform.exit());
        return quitGameButton;
    }

    private Pile createDiscardPile() {
        discardPile = createPile("discard", 0, 0);
        return discardPile;
    }

    private CardView createCardView(Card card) {
        CardView cardView = new CardView(card, CARD_WIDTH, CARD_HEIGHT);
        cardView.turnFaceUp();
        makeDraggable(cardView);
        return cardView;
    }

    private void gameOver() {
        disableGame();
        show(gameOverNode);
    }

    private void disableGame() {
        getAllPiles().forEach(this::disablePile);
        show(gameOverNode);
    }

    private void enableGame() {
        getAllPiles().forEach(this::enablePile);
        hide(gameOverNode);
    }

    private List<Pile> getAllPiles() {
        return Stream.of(tableauPiles, foundationPiles, reservePiles, Arrays.asList(stock, discardPile))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private void disablePile(Pile pile) {
        pile.setDisable(true);
        pile.setOpacity(0.4);
    }

    private void enablePile(Pile pile) {
        pile.setDisable(false);
        pile.setOpacity(1);
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
