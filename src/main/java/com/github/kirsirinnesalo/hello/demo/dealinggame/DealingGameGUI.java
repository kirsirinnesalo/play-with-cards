package com.github.kirsirinnesalo.cards.demo.dealinggame;

import com.github.kirsirinnesalo.cards.Deck;
import com.github.kirsirinnesalo.cards.control.DeckPane;
import com.github.kirsirinnesalo.cards.control.HandPane;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class DealingGameGUI extends Application {

    private static final String GAME_TITLE = "Dealing Game";
    private static final int CARDS_PER_HAND = 13;
    private static final int NUMBER_OF_PLAYERS = 2;
    private static final int TABLE_WIDTH = 700;
    private static final int TABLE_HEIGHT = 400;
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    private DealingGame game;

    public DealingGameGUI() {
        this.game = new DealingGame(NUMBER_OF_PLAYERS, CARDS_PER_HAND);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(GAME_TITLE);
        stage.setScene(createScene());
        stage.centerOnScreen();
        stage.show();
    }

    private Scene createScene() {
        BorderPane table = new BorderPane();
        table.setBackground(Utils.TRANSPARENT_BACKGROUND);

        table.setTop(createHeader());
        table.setLeft(createDeckComponent());
        table.setCenter(createPlayerPanes());

        return new Scene(table, TABLE_WIDTH, TABLE_HEIGHT);
    }

    private HBox createHeader() {
        Text text = new Text(GAME_TITLE);
        text.setFont(new Font(20));
        HBox box = new HBox(20, text);
        box.setAlignment(Pos.BASELINE_CENTER);
        return box;
    }

    private VBox createDeckComponent() {
        Insets padding = new Insets(10, 10, 10, 10);
        Insets margin = new Insets(10, 10, 10, 10);

        Label label = new Label(game.getDeck().getLabel());
        label.setPadding(padding);
        label.setAlignment(Pos.TOP_CENTER);

        DeckPane deckPane = new DeckPane(game.getDeck(), false, CARD_WIDTH * 1.5, CARD_HEIGHT * 1.5);
        deckPane.setAlignment(Pos.BASELINE_CENTER);
        deckPane.setOnMouseClicked((MouseEvent event) -> {
            if (game.enoughCardsInDeckForRound()) {
                dealRound();
            } else {
                resetGame();
            }
            label.setText(game.getDeck().getLabel());
        });
        deckPane.setOnMouseEntered(e -> deckPane.setCursor(Cursor.HAND));
        game.getDeck().shuffle();

        Label helpText = new Label("Deal by clicking the deck. Deck resets if there aren't enough cards to deal.");
        helpText.setPadding(new Insets(30, 0, 0, 0));
        helpText.setWrapText(true);

        VBox box = new VBox();
        box.getChildren().addAll(label, deckPane, helpText);
        box.setPadding(padding);
        box.setPrefWidth(150);
        BorderPane.setMargin(box, margin);
        box.setBackground(Utils.getBackgroundWith(Color.BISQUE));
        return box;
    }

    private void resetGame() {
        Deck deck = game.getDeck();
        game.getPlayers().forEach(player -> player.getHand().clear());
        deck.reset();
        deck.shuffle();
    }

    private void dealRound() {
        game.getPlayers().forEach(player -> game.dealHandFor(player));
    }

    private GridPane createPlayerPanes() {
        GridPane pane = new GridPane();
        pane.setBackground(Utils.TRANSPARENT_BACKGROUND);
        AtomicInteger playerRow = new AtomicInteger(0);
        game.getPlayers().forEach(player -> {
            Pane playerPane = new HandPane(player.getName(), player.getHand(), CARD_WIDTH, CARD_HEIGHT);
            GridPane.setMargin(playerPane, new Insets(5, 0, 1, 3));
            playerPane.setPrefWidth(500);
            pane.add(playerPane, 0, playerRow.getAndAdd(1));
        });
        pane.setAlignment(Pos.BASELINE_LEFT);

        return pane;
    }

}
