package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Player;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.LongAdder;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackGame.*;

public class BlackjackPlayer extends Player {
    static final BlackjackPlayer NOBODY = new BlackjackPlayer("Nobody", 0);
    private IntegerProperty money = new SimpleIntegerProperty(0);

    private boolean gameOver = false;

    BlackjackPlayer(String name, int money) {
        super(name);
        this.money.set(money);
    }

    public boolean needsMoreCards() {
        return !gameOver && countSum() < BLACKJACK;
    }

    int countSum() {
        final LongAdder sum = new LongAdder();

        ObservableList<Card> hand = getHand();
        hand.sort(Comparator.comparing(card -> card.rank));
        hand.sort(Collections.reverseOrder());

        hand.forEach(card -> {
            if (card.isFaceCard()) {
                sum.add(FACE_CARD_VALUE);
            } else if (card.isAce()) {
                sum.add(aceValue(sum.intValue()));
            } else {
                sum.add(card.rank.numericValue());
            }
        });
        return sum.intValue();
    }

    private int aceValue(int sum) {
        if (sum + ACE_VALUE_BIG > BLACKJACK) {
            return ACE_VALUE_SMALL;
        } else {
            return ACE_VALUE_BIG;
        }
    }

    boolean hasBlackjack() {
        return getHand().size() == 2 && countSum() == BLACKJACK;
    }

    void quitGame() {
        gameOver = true;
    }

    boolean isGameOver() {
        return gameOver;
    }

    boolean isBusted() {
        return countSum() > BLACKJACK;
    }

    private String handSum() {
        int sum = countSum();
        String handSum = " => " + sum;
        if (hasBlackjack()) {
            handSum += " BLACKJACK";
        } else if (sum > BLACKJACK) {
            handSum += " BUSTED";
        }
        return handSum;
    }

    @Override
    public String toString() {
        return super.toString() + handSum();
    }

    @Override
    public ObservableList<Card> resetHand() {
        gameOver = false;
        return super.resetHand();
    }

    void addMoney(int amount) {
        money.set(money.get() + amount);
    }

    IntegerProperty getMoneyProperty() {
        return money;
    }

    int getMoney() {
        return money.intValue();
    }
}
