package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.stream.Stream;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.Phase.BETTING;
import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.Phase.PLAYER_TURN;
import static com.github.kirsirinnesalo.cards.blackjack.BlackjackPlayer.NOBODY;
import static java.util.stream.IntStream.rangeClosed;

class BlackjackGame implements Game {

    static final int BLACKJACK = 21;
    static final int FACE_CARD_VALUE = 10;

    private BlackjackDealer dealer;
    private BlackjackPlayer player;

    private Deck deck;
    private IntegerProperty bet = new SimpleIntegerProperty(0);
    private Phase phase;

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

        newRound();
        setPhase(BETTING);
    }

    void newRound() {
        clearHands();
        deck.reset();
        deck.shuffle();
        bet.setValue(0);
    }

    void clearHands() {
        Stream.of(dealer, player).forEach(BlackjackPlayer::clearHands);
    }

    Deck getDeck() {
        return deck;
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

    Phase getPhase() {
        return phase;
    }

    void setPhase(Phase phase) {
        this.phase = phase;
    }

    void deal() {
        rangeClosed(1, 2).forEach($ -> {
            hit(getDealer());
            hit(getPlayer());
        });
        setPhase(PLAYER_TURN);
    }

    void hit(BlackjackPlayer player) {
        Card card = deck.dealCard();
        player.takeCard(card);
        if (player.isBusted() || player.hasBlackjack()) {
            player.closeHand();
        }
    }

    Player resolveWinner() {
        BlackjackDealer currentDealer = getDealer();
        BlackjackPlayer currentPlayer = getPlayer();
        int dealerHandSum = currentDealer.getCurrentHandSum();
        int playerHandSum = currentPlayer.getCurrentHandSum();
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
        } else if (dealerHandSum == playerHandSum) {
            return NOBODY;
        }
        return dealerHandSum > playerHandSum ? currentDealer : currentPlayer;
    }

    void autoPlay(BlackjackPlayer player) {
        while (player.needsMoreCards()) {
            hit(player);
        }
    }

    void payBet(Player winner) {
        if (!NOBODY.equals(winner)) {
            BlackjackPlayer currentPlayer = getPlayer();
            int winning = getWinning(winner, currentPlayer);
            currentPlayer.addMoney(winning);
        }
    }

    int getWinning(Player winner, BlackjackPlayer player) {
        int winning = bet.intValue();
        if (player.equals(winner)) {
            if (player.hasBlackjack()) {
                winning = (int) (bet.intValue() * 1.5);
            }
        } else {
            winning = -bet.intValue();
        }
        return winning;
    }

    enum Phase {
        NEW_GAME,
        NEW_ROUND,
        BETTING,
        PLAYER_TURN,
        DEALER_TURN,
        ROUND_OVER,
        GAME_OVER
    }
}
