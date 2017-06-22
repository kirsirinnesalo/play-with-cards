package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.cards.control.CardView;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.BLACKJACK;

public class BlackjackGUI extends Application {

    private static final String GAME_TITLE = "Blackjack";
    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = 160;
    private static final int PLAYER_PANE_WIDTH = CARD_WIDTH * 3;
    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 600;
    private static final Font HEADER_FONT = new Font("Arial Black", 30);
    private static final Font MONEY_FONT = new Font("Arial Black", 12);

    private BlackjackGame game;
    private PlayerPane dealerPane;
    private Button standButton;
    private Button doubleButton;
    private Button splitButton;
    private Button betButton;
    private Node betNode;
    private TextField betField;
    private HBox currentBet;
    private Label currentBetLabel;
    private Label walletLabel;

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
        show(betNode);

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
        VBox pane = new VBox();
        pane.setBackground(Utils.getBackgroundWith(Color.WHITESMOKE));
        pane.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #e0e0e0;");
        pane.setAlignment(Pos.TOP_CENTER);

        dealerPane = new PlayerPane(game.getDealer(), CARD_WIDTH, CARD_HEIGHT);
        dealerPane.setPrefWidth(PLAYER_PANE_WIDTH);

        PlayerPane playerPane = new PlayerPane(game.getPlayer(), CARD_WIDTH, CARD_HEIGHT);
        playerPane.setPrefWidth(PLAYER_PANE_WIDTH);

        pane.getChildren().addAll(dealerPane, createCurrentBetLabel(), createBetNode(), playerPane, createWalletLabel());
        return pane;
    }

    private Node createBetNode() {
        betField = new TextField("100");
        betField.setPrefWidth(50);
        Label betLabel = new Label("Bet sum: ");
        betLabel.setLabelFor(betField);
        betButton = new Button("Bet");

        betNode = createHBox(betLabel, betField, betButton);
        return betNode;
    }

    private HBox createHBox(Label label, TextField field, Button button) {
        HBox box = new HBox();
        box.setAlignment(Pos.BASELINE_CENTER);
        box.setSpacing(5);
        box.getChildren().addAll(label, field, button);
        return box;
    }

    private HBox createCurrentBetLabel() {
        currentBetLabel = createMoneyLabel(String.valueOf(game.getBet()));
        currentBet = createMoneyLabelWith("Current bet: ", currentBetLabel);
        currentBet.setPadding(new Insets(10));
        return currentBet;
    }

    private HBox createWalletLabel() {
        walletLabel = createMoneyLabel(String.valueOf(game.getPlayer().getMoney()));
        walletLabel.setPadding(new Insets(10));
        return createMoneyLabelWith("Wallet: ", walletLabel);
    }

    private HBox createMoneyLabelWith(String text, Label label) {
        HBox box = new HBox();
        box.getChildren().addAll(new Text(text), label, new Text(" €"));
        box.setAlignment(Pos.BASELINE_CENTER);
        return box;
    }

    private Label createMoneyLabel(String text) {
        Label moneyLabel = new Label(text);
        moneyLabel.setFont(MONEY_FONT);
        return moneyLabel;
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
        bindVisibleProperties();
        bindMoneyLabels();
        setButtonActions();
    }

    private void bindVisibleProperties() {
        standButton.managedProperty().bind(standButton.visibleProperty());
        doubleButton.managedProperty().bind(doubleButton.visibleProperty());
        splitButton.managedProperty().bind(splitButton.visibleProperty());
        betNode.managedProperty().bind(betNode.visibleProperty());
        currentBet.managedProperty().bind(currentBet.visibleProperty());

        dealerPane.getRevealHiddenCardProperty().addListener((observable, oldValue, revealCard) -> {
            CardView cardView = (CardView) dealerPane.getCardPane().getChildren().get(1);
            if (revealCard) {
                cardView.turnFaceUp();
            } else {
                cardView.turnFaceDown();
            }
        });
    }

    private void bindMoneyLabels() {
        walletLabel.textProperty().bind(game.getPlayer().getMoneyProperty().asString());
        currentBetLabel.textProperty().bind(game.getBetProperty().asString());
    }

    private void setButtonActions() {
        betButton.setOnAction($ -> bet());
        doubleButton.setOnAction($ -> doubleDown());
        standButton.setOnAction($ -> stand());
    }

    private void autoPlay(BlackjackDealer dealer) {
        dealerPane.revealHiddenCard();
        if (!dealer.hasBlackjack()) {
            game.autoPlay(dealer);
        }
        gameOver();
    }

    private void deal() {
        game.getDeck().shuffle();
        game.deal();
        dealerPane.hideHiddenCard();
        BlackjackPlayer player = game.getPlayer();
        if (game.getDealer().hasBlackjack() || player.hasBlackjack()) {
            dealerPane.revealHiddenCard();
            gameOver();
        } else {
            show(standButton, doubleButton, currentBet);
            hide(betNode);
            List<Card> hand = player.getHand();
            if (player.valueFor(hand.get(0)) == player.valueFor(hand.get(1))) {
                show(splitButton);
            }
        }
    }

    private void hit(BlackjackPlayer player) {
        if (player.needsMoreCards()) {
            game.hit(player);
            if (player.countSum() == BLACKJACK) {
                stand();
            }
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
            dealerPane.revealHiddenCard();
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
