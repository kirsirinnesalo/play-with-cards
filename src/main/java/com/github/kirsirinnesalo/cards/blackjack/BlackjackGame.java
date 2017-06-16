package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.stream.Stream;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackPlayer.NOBODY;
import static java.util.stream.IntStream.rangeClosed;

class BlackjackGame implements Game {

    static final int BLACKJACK = 21;
    static final int FACE_CARD_VALUE = 10;
    static final int ACE_VALUE_BIG = 11;
    static final int ACE_VALUE_SMALL = 1;

    private BlackjackDealer dealer;
    private BlackjackPlayer player;

    private Deck deck;
    private boolean gameInProgress = false;
    private IntegerProperty bet = new SimpleIntegerProperty(0);

    BlackjackGame() {
        setupTable();
    }

    BlackjackDealer getDealer() {
        return dealer;
    }

    BlackjackPlayer getPlayer() {
        return player;
    }

    private void setupTable() {
        deck = new Deck52();

        dealer = new BlackjackDealer("Dealer", 17);
        player = new BlackjackPlayer("Player", 1000);

        gameInProgress = false;
    }

    void resetGame() {
        Stream.of(dealer, player).forEach(p -> deck.addAll(p.resetHand()));
        deck.shuffle();
        gameInProgress = false;
        bet.setValue(0);
    }

    Deck getDeck() {
        return deck;
    }

    boolean isGameInProgress() {
        return gameInProgress;
    }

    IntegerProperty getBetProperty() {
        return bet;
    }

    int getBet() {
        return bet.intValue();
    }

    void setBet(int bet) {
        this.bet.setValue(bet);
    }

    void deal() {
        rangeClosed(1, 2).forEach($ -> {
            hit(getDealer());
            hit(getPlayer());
        });
        gameInProgress = true;
    }

    void hit(BlackjackPlayer player) {
        Card card = deck.dealCard();
        player.takeCard(card);
        if (player.isBusted() || player.hasBlackjack()) {
            player.quitGame();
        }
    }

    Player resolveWinner() {
        BlackjackDealer currentDealer = getDealer();
        BlackjackPlayer currentPlayer = getPlayer();
        if (currentDealer.hasBlackjack()) {
            if (currentPlayer.hasBlackjack()) {
                return NOBODY;
            }
            return currentDealer;
        } else if (currentPlayer.hasBlackjack()) {
            return currentPlayer;
        } else if (currentDealer.isBusted()) {
            if (currentPlayer.isBusted()) {
                return NOBODY;
            }
            return currentPlayer;
        } else if (currentPlayer.isBusted()) {
            return currentDealer;
        } else if (currentDealer.countSum() == currentPlayer.countSum()) {
            return NOBODY;
        }
        return currentDealer.countSum() > currentPlayer.countSum() ? currentDealer : currentPlayer;
    }

    void autoPlay(BlackjackPlayer player) {
        while (player.needsMoreCards()) {
            hit(player);
        }
    }

    void payBet(Player winner) {
        if (!NOBODY.equals(winner)) {
            BlackjackPlayer currentPlayer = getPlayer();
            int winning = bet.intValue();
            if (currentPlayer.equals(winner)) {
                if (currentPlayer.hasBlackjack()) {
                    winning = (int) (bet.intValue() * 1.5);
                }
            } else {
                winning = -bet.intValue();
            }
            currentPlayer.addMoney(winning);
        }
    }

}
