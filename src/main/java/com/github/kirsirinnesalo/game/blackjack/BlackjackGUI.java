package com.github.kirsirinnesalo.game.blackjack;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.DeckPane;
import com.github.kirsirinnesalo.control.HandPane;
import com.github.kirsirinnesalo.game.FXGameApplication;
import com.github.kirsirinnesalo.model.Player;
import com.github.kirsirinnesalo.scene.util.Utils;

import org.apache.commons.lang3.StringUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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

import java.util.Arrays;
import java.util.OptionalInt;

import static com.github.kirsirinnesalo.game.blackjack.BlackjackGame.BLACKJACK;
import static com.github.kirsirinnesalo.game.blackjack.BlackjackGame.Phase.*;
import static com.github.kirsirinnesalo.game.blackjack.BlackjackPlayer.NOBODY;

public class BlackjackGUI extends FXGameApplication {

    private static final String GAME_TITLE = "Blackjack";
    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = 160;
    private static final int PLAYER_PANE_WIDTH = CARD_WIDTH * 3;
    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 600;
    private static final Font MONEY_FONT = new Font("Arial Black", 12);

    private BlackjackGame game;
    private HandPane dealerPane;
    private HandPane playerPane;
    private Button standButton;
    private Button doubleButton;
    private Button splitButton;
    private Button betButton;
    private Node betNode;
    private TextField betField;
    private HBox currentBet;
    private Label currentBetLabel;
    private Label walletLabel;
    private HBox walletNode;
    private TextFlow message;

    public BlackjackGUI() {
        this.game = new BlackjackGame();
    }

    @Override
    public String getTitle() {
        return GAME_TITLE;
    }

    @Override
    public double getWidth() {
        return TABLE_WIDTH;
    }

    @Override
    public double getHeight() {
        return TABLE_HEIGHT;
    }

    @Override
    public Parent createGameTable() {
        BorderPane table = new BorderPane();
        table.setBackground(Utils.getBackgroundWith(Color.DARKGREEN));
        table.setTop(createHeader());
        table.setBottom(createFooter());
        table.setLeft(createLeftBar());
        table.setRight(createRightBar());
        table.setCenter(createGamePane());

        initBindings();
        hide(standButton, splitButton, doubleButton);
        show(betNode);

        return table;
    }

    @Override
    protected Image getApplicationIcon() {
        return Utils.getImage("icons/cardicon.png");
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


    private Node createGamePane() {
        VBox pane = new VBox();
        pane.setBackground(Utils.getBackgroundWith(Color.WHITESMOKE));
        pane.setStyle("-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: #e0e0e0;");
        pane.setAlignment(Pos.TOP_CENTER);

        BlackjackDealer dealer = game.getDealer();
        dealerPane = new HandPane(dealer.getName(), dealer.getHand(), CARD_WIDTH, CARD_HEIGHT);
        dealerPane.setPrefWidth(PLAYER_PANE_WIDTH);

        BlackjackPlayer player = game.getPlayer();
        playerPane = new HandPane(player.getName(), player.getHand(), CARD_WIDTH, CARD_HEIGHT);
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
        walletNode = createMoneyLabelWith("Wallet: ", walletLabel);
        return walletNode;
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
        dealerPane.managedProperty().bind(dealerPane.visibleProperty());
        playerPane.managedProperty().bind(playerPane.visibleProperty());
        walletNode.managedProperty().bind(walletNode.visibleProperty());

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
                newGame();
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

    private void newGame() {
        show(dealerPane, playerPane, walletNode);
        game.getPlayer().setMoney(1000);
        newRound();
    }

    private void newRound() {
        clearMessage();
        hide(standButton, splitButton, doubleButton);
        show(currentBet, betNode);
        game.newRound();
        game.setPhase(BETTING);
    }

    private void autoPlay(BlackjackDealer dealer) {
        dealerPane.revealHiddenCard();
        if (!dealer.hasBlackjack() && !game.getPlayer().isBusted()) {
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
            hide(betNode);
            int bet = game.getBet();
            if (player.canDoubleOrSplit(bet)) {
                show(doubleButton);
            }
            if (player.canSplit(bet)) {
                show(splitButton);
            }
        }
    }

    private void hit(BlackjackPlayer player) {
        if (player.needsMoreCards()) {
            game.hit(player);
            if (player.getCurrentHandSum() == BLACKJACK) {
                stand();
            } else if (player.isBusted()) {
                player.closeHand();
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
        game.getPlayer().closeHand();
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
        game.clearHands();
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
