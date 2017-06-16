package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.cards.control.DeckPane;
import com.github.kirsirinnesalo.cards.control.PlayerPane;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

public class BlackjackGUI extends Application {

    private static final String GAME_TITLE = "Blackjack";
    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = 160;
    private static final int PLAYER_PANE_WIDTH = CARD_WIDTH * 11;
    private static final int PLAYER_PANE_HEIGHT = CARD_HEIGHT + 20;
    private static final int TABLE_WIDTH = 1000;
    private static final int TABLE_HEIGHT = 600;
    private static final Font HEADER_FONT = new Font("Arial Black", 30);
    private static final Font MONEY_FONT = new Font("Arial Black", 12);

    private BlackjackGame game;
    private Button standButton;
    private Button doubleButton;
    private Button splitButton;
    private Button betButton;
    private Node betNode;
    private TextField betField;
    private Label currentBetAmount;
    private Label moneyAmount;

    public BlackjackGUI() {
        this.game = new BlackjackGame();
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
        table.setBackground(Utils.getBackgroundWith(Color.DARKGREEN));
        table.setTop(createHeader());
        table.setBottom(createFooter());
        table.setLeft(createLeftBar());
        table.setRight(createRightBar());
        table.setCenter(createGameTable());

        initBindings();
        hide(standButton, splitButton, doubleButton);
        betNode.setVisible(true);

        return new Scene(table, TABLE_WIDTH, TABLE_HEIGHT);
    }

    private Node createLeftBar() {
        VBox box = new VBox();
        box.setPrefWidth(150);
        box.setMinHeight(20);
        box.setSpacing(50);
        box.setPadding(new Insets(CARD_HEIGHT, 10, 10, 10));
        box.getChildren().addAll(createDeckPane(), createGameButtons());
        return box;
    }

    private Node createDeckPane() {
        DeckPane deckPane = new DeckPane(game.getDeck(), false, CARD_WIDTH, CARD_HEIGHT);
        deckPane.setOnMouseClicked((MouseEvent event) -> {
            BlackjackPlayer player = game.getPlayer();
            BlackjackDealer dealer = game.getDealer();
            if (game.isGameInProgress()) {
                hit(player);
            } else {
                if (game.getBetProperty().intValue() > 0) {
                    deal();
                } else {
                    showWarning("Set the bet first.");
                }
            }
            if (player.isGameOver()) {
                autoPlay(dealer);
            }
        });
        deckPane.setOnMouseEntered(e -> deckPane.setCursor(Cursor.HAND));
        return deckPane;
    }

    private void showWarning(String warning) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setHeaderText(warning);
        alert.show();
    }

    private Node createGameButtons() {
        VBox box = new VBox();
        box.setSpacing(10);
        box.setAlignment(Pos.BASELINE_CENTER);

        standButton = new Button("Stand");
        doubleButton = new Button("Double");
        splitButton = new Button("Split");

        box.getChildren().addAll(standButton, doubleButton, splitButton);
        return box;
    }


    private Node createGameTable() {
        GridPane pane = new GridPane();
        pane.setBackground(Utils.getBackgroundWith(Color.WHITESMOKE));
        pane.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #e0e0e0;");

        PlayerPane dealerPane = new PlayerPane(game.getDealer(), PLAYER_PANE_WIDTH, PLAYER_PANE_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
        pane.add(dealerPane, 0, 0);
        GridPane.setColumnSpan(dealerPane, 2);

        pane.setHgap(50);
        pane.add(createMoneyLabels(), 0, 1);
        pane.add(createBettingPane(), 1, 1);

        PlayerPane playerPane = new PlayerPane(game.getPlayer(), PLAYER_PANE_WIDTH, PLAYER_PANE_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
        pane.add(playerPane, 0, 2);
        GridPane.setColumnSpan(playerPane, 2);

        return pane;
    }

    private Node createBettingPane() {
        betNode = createBetNode();

        GridPane box = new GridPane();
        box.setPadding(new Insets(5, 5, 5, 20));
        box.setHgap(50);
        box.add(betNode, 1, 0);
        return box;
    }

    private Node createBetNode() {
        betField = new TextField("100");
        betField.setPrefWidth(50);
        Label betLabel = new Label("Bet sum: ");
        betLabel.setLabelFor(betField);
        betButton = new Button("Bet");

        return createHBox(betLabel, betField, betButton);
    }

    private HBox createHBox(Label label, TextField field, Button button) {
        HBox box = new HBox();
        box.setAlignment(Pos.BASELINE_CENTER);
        box.setSpacing(5);
        box.getChildren().addAll(label, field, button);
        return box;
    }

    private Node createMoneyLabels() {
        VBox moneyBox = new VBox();
        GridPane.setMargin(moneyBox, new Insets(0, 0, 0, 50));
        moneyBox.setAlignment(Pos.BASELINE_LEFT);
        moneyBox.setSpacing(10);
        moneyBox.getChildren().addAll(createWalletLabel(), createCurrentBetLabel());
        return moneyBox;
    }

    private Node createCurrentBetLabel() {
        currentBetAmount = createMoneyLabel(game.getBet());
        return createLabelPair("Current bet: ", currentBetAmount);
    }

    private Node createWalletLabel() {
        moneyAmount = createMoneyLabel(game.getPlayer().getMoney());
        return createLabelPair("Wallet: ", moneyAmount);
    }

    private Label createMoneyLabel(int amount) {
        Label moneyLabel = new Label(String.valueOf(amount));
        moneyLabel.setFont(MONEY_FONT);
        moneyLabel.setPrefWidth(50);
        moneyLabel.setTextAlignment(TextAlignment.RIGHT);
        return moneyLabel;
    }

    private Node createLabelPair(String amountLabelText, Label currentAmount) {
        HBox currentBet = new HBox();
        currentBet.setAlignment(Pos.BASELINE_RIGHT);
        currentBet.getChildren().addAll(new Text(amountLabelText), currentAmount, new Text(" €"));
        return currentBet;
    }

    private Node createHeader() {
        Label headerLabel = new Label(GAME_TITLE);
        headerLabel.setFont(HEADER_FONT);
        HBox header = new HBox(headerLabel);
        header.setAlignment(Pos.TOP_CENTER);
        header.setPadding(new Insets(20));
        return createEmptyHBox();
    }

    private Node createRightBar() {
        VBox box = new VBox();
        box.setPrefWidth(20);
        return box;
    }

    private Node createFooter() {
        return createEmptyHBox();
    }

    private Node createEmptyHBox() {
        HBox box = new HBox();
        box.setPrefHeight(20);
        return box;
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

    private void initBindings() {
        bindVisbleProperties();
        bindMoneyLabels();
        setButtonActions();
    }

    private void bindVisbleProperties() {
        standButton.managedProperty().bind(standButton.visibleProperty());
        doubleButton.managedProperty().bind(doubleButton.visibleProperty());
        splitButton.managedProperty().bind(splitButton.visibleProperty());
        betNode.managedProperty().bind(betNode.visibleProperty());
    }

    private void bindMoneyLabels() {
        moneyAmount.textProperty().bind(game.getPlayer().getMoneyProperty().asString());
        currentBetAmount.textProperty().bind(game.getBetProperty().asString());
    }

    private void setButtonActions() {
        betButton.setOnAction($ -> bet());
        doubleButton.setOnAction($ -> doubleDown());
        standButton.setOnAction($ -> stand());
    }

    private void autoPlay(BlackjackDealer dealer) {
        if (!dealer.hasBlackjack()) {
            game.autoPlay(dealer);
        }
        gameOver();
    }

    private void deal() {
        game.getDeck().shuffle();
        game.deal();
        if (game.getDealer().hasBlackjack() || game.getPlayer().hasBlackjack()) {
            gameOver();
        } else {
            show(standButton, splitButton, doubleButton);
            hide(betNode);
        }
    }

    private void hit(BlackjackPlayer player) {
        if (player.needsMoreCards()) {
            game.hit(player);
        }
        splitButton.setVisible(false);
        doubleButton.setVisible(false);
    }

    private void bet() {
        if (!game.isGameInProgress()) {
            int money = game.getPlayer().getMoney();
            OptionalInt betValue = OptionalInt.of(Integer.valueOf(betField.getText()));
            if (betValue.orElse(0) <= money) {
                game.setBet(betValue.getAsInt());
            } else {
                if (money > 0) {
                    betField.setText(String.valueOf(money));
                }
            }
        }
    }

    private void doubleDown() {
        int doubledBet = game.getBet() * 2;
        if (doubledBet <= game.getPlayer().getMoney()) {
            game.setBet(doubledBet);
            hit(game.getPlayer());
            stand();
        } else {
            showWarning("You don't have enough money to double down.");
        }
    }

    private void stand() {
        if (game.isGameInProgress()) {
            game.getPlayer().quitGame();
            game.autoPlay(game.getDealer());
            gameOver();
        }
    }

    private void gameOver() {
        Player winner = game.resolveWinner();
        game.payBet(winner);
        Alert dialog = new Alert(AlertType.CONFIRMATION);
        dialog.setTitle("Round Over");
        dialog.setHeaderText("Round Over!");
        dialog.setContentText(game.getDealer().toString() + "\n"
                + game.getPlayer().toString() + "\n"
                + winner.getName() + " won."
                + "\n\n"
                + "You have now " + game.getPlayer().getMoney() + " €."
                + "\n\n"
                + "Deal again?"
        );
        Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (ButtonType.OK == buttonType) {
                hide(standButton, splitButton, doubleButton);
                show(betNode);
                game.resetGame();
            } else {
                Platform.exit();
            }
        });
    }

}
