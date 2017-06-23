package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Player;
import com.github.kirsirinnesalo.cards.control.CardView;
import com.github.kirsirinnesalo.cards.control.DeckPane;
import com.github.kirsirinnesalo.cards.control.PlayerPane;
import com.github.kirsirinnesalo.scene.util.Utils;

import org.apache.commons.lang3.StringUtils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.BLACKJACK;
import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.Phase.*;
import static com.github.kirsirinnesalo.cards.blackjack.BlackjackPlayer.NOBODY;

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
    private TextFlow message;

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
        deckPane.setOnMouseClicked((MouseEvent event) -> playGame());
        deckPane.setOnMouseEntered(e -> deckPane.setCursor(Cursor.HAND));
        return deckPane;
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

        pane.getChildren().addAll(dealerPane, createMessagePane(), createCurrentBetLabel(), createBetNode(), playerPane, createWalletLabel());
        return pane;
    }

    private TextFlow createMessagePane() {
        message = new TextFlow();
        message.setTextAlignment(TextAlignment.CENTER);
        return message;
    }

    private void clearMessage() {
        message.getChildren().clear();
        hide(message);
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
        message.managedProperty().bind(message.visibleProperty());
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
        betField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                betField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setButtonActions() {
        betField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                bet();
            }
        });
        betButton.setOnAction($ -> bet());
        doubleButton.setOnAction($ -> doubleDown());
        standButton.setOnAction($ -> stand());
        splitButton.setOnAction($ -> split());
    }

    private void playGame() {
        switch (game.getPhase()) {
            case NEW_GAME:
                game.getPlayer().setMoney(1000);
                newRound();
                break;
            case NEW_ROUND:
                newRound();
                break;
            case BETTING:
                if (game.getBet() > 0) {
                    deal();
                } else {
                    showMessage("Set the bet first.");
                }
                break;
            case PLAYER_TURN:
                hit(game.getPlayer());
                break;
            case DEALER_TURN:
                autoPlay(game.getDealer());
                break;
            case GAME_OVER:
                gameOver();
                break;
            default:
                break;
        }
    }

    private void showMessage(String text) {
        showMessage(new Text(text));
    }

    private void showMessage(Node... messages) {
        clearMessage();
        message.getChildren().addAll(messages);
        show(message);
    }

    private void newRound() {
        clearMessage();
        hide(standButton, splitButton, doubleButton);
        show(currentBet, betNode);
        game.setPhase(BETTING);
        game.newRound();
    }

    private void autoPlay(BlackjackDealer dealer) {
        dealerPane.revealHiddenCard();
        if (!dealer.hasBlackjack()) {
            game.autoPlay(dealer);
        }
        roundOver();
    }

    private void deal() {
        game.getDeck().shuffle();
        game.deal();
        dealerPane.hideHiddenCard();
        BlackjackPlayer player = game.getPlayer();
        if (game.getDealer().hasBlackjack() || player.hasBlackjack()) {
            dealerPane.revealHiddenCard();
            roundOver();
        } else {
            game.setPhase(PLAYER_TURN);
            show(standButton, currentBet);
            if (player.canDouble(game.getBet())) {
                show(doubleButton);
            }
            hide(betNode);
            List<Card> hand = player.getHand();
            if (player.canDouble(game.getBet()) && player.valueFor(hand.get(0)) == player.valueFor(hand.get(1))) {
                show(splitButton);
            }
        }
    }

    private void hit(BlackjackPlayer player) {
        if (player.needsMoreCards()) {
            game.hit(player);
            if (player.countSum() == BLACKJACK) {
                stand();
            } else if (player.isBusted()) {
                player.quitRound();
                game.setPhase(DEALER_TURN);
                autoPlay(game.getDealer());
            }
        }
        hide(splitButton, doubleButton);
    }

    private void bet() {
        if (!StringUtils.isEmpty(betField.getText())) {
            int money = game.getPlayer().getMoney();
            OptionalInt betValue = OptionalInt.of(Integer.valueOf(betField.getText()));
            if (betValue.orElse(0) <= money) {
                game.setBet(betValue.getAsInt());
            } else {
                if (money > 0) {
                    betField.setText(String.valueOf(money));
                }
            }
            clearMessage();
        }
    }

    private void doubleDown() {
        game.setBet(game.getBet() * 2);
        hit(game.getPlayer());
        stand();
    }

    private void split() {
        showMessage("Split is not implemented yet.");
    }

    private void stand() {
        game.getPlayer().quitRound();
        dealerPane.revealHiddenCard();
        game.autoPlay(game.getDealer());
        game.setPhase(ROUND_OVER);
        roundOver();
    }

    private void roundOver() {
        Player winner = game.resolveWinner();
        game.payBet(winner);
        hide(currentBet, betNode, standButton, doubleButton, splitButton);
        showMessage(createRoundOverText(winner));
        if (game.getPlayer().getMoney() > 0) {
            game.setPhase(NEW_ROUND);
        } else {
            game.setPhase(GAME_OVER);
        }
    }

    private void gameOver() {
        game.resetDeck();
        showMessage(createText("GAME OVER.", "-fx-font-weight: bold; -fx-font-size: large;"),
                new Text(" You lost all your money. Start a new game?"));
        game.setPhase(NEW_GAME);
    }

    private Node[] createRoundOverText(Player winner) {
        return new Node[]{createText("Round over.", "-fx-font-weight: bold;"),
                createWinnerText(winner),
                createText(showWinning(winner), "-fx-font-style: italic;")};
    }

    private String showWinning(Player winner) {
        if (!NOBODY.equals(winner)) {
            int winning = game.getWinning(winner, game.getPlayer());
            return "  " + (winning > 0 ? "+" : "") + winning + " €";
        }
        return StringUtils.EMPTY;
    }

    private Text createWinnerText(Player winner) {
        String winnerText;
        String style = "-fx-font-size: 2em; -fx-font-weight: bold;";
        if (NOBODY.equals(winner)) {
            if (game.getPlayer().isBusted()) {
                winnerText = " Both busted!";
            } else {
                winnerText = " It's a tie!";
            }
        } else if (game.getDealer().equals(winner)) {
            winnerText = " You lost.";
            style += " -fx-fill: red;";
        } else {
            winnerText = " You won!";
            style += " -fx-fill: green; -fx-font-style: italic;";
        }
        Text text = new Text(winnerText);
        text.setStyle(style);
        return text;
    }

    private Text createText(String text, String style) {
        Text textNode = new Text();
        if (!StringUtils.isEmpty(text)) {
            textNode.setText(text);
            textNode.setStyle(style);
        }
        return textNode;
    }

}
