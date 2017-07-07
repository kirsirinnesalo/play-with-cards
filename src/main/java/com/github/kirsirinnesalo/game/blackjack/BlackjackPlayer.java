package com.github.kirsirinnesalo.game.blackjack;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Hand;
import com.github.kirsirinnesalo.model.Player;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static com.github.kirsirinnesalo.model.Card.Rank.ACE;
import static com.github.kirsirinnesalo.game.blackjack.BlackjackGame.BLACKJACK;
import static com.github.kirsirinnesalo.game.blackjack.BlackjackGame.FACE_CARD_VALUE;

public class BlackjackPlayer extends Player {
    static final BlackjackPlayer NOBODY = new BlackjackPlayer("Nobody", 0);

    private final List<Hand> hands;
    private Hand currentHand;
    private IntegerProperty money = new SimpleIntegerProperty(0);
    private IntegerProperty currentHandSum = new SimpleIntegerProperty();

    BlackjackPlayer(String name, int money) {
        super(name);
        this.money.set(money);
        hands = new ArrayList<>(2);
        hands.add(currentHand = new Hand());
        currentHandSum.set(0);
        currentHand.getCards().addListener((ListChangeListener<Card>) c -> currentHandSum.set(countSum()));
    }

    private int countSum() {
        final LongAdder sum = new LongAdder();
        List<Card> cards = currentHand.getCards();
        cards.forEach(card -> sum.add(valueFor(card)));
        long numOfAces = cards.stream().filter(card -> ACE.equals(card.rank)).count();
        while (numOfAces > 0) {
            if (sum.intValue() > BLACKJACK) {
                sum.add(-10);
                numOfAces--;
            } else {
                break;
            }
        }
        return sum.intValue();
    }

    @Override
    public Hand getHand() {
        return currentHand;
    }

    int getCurrentHandSum() {
        return currentHandSum.get();
    }

    public boolean needsMoreCards() {
        return currentHand.isOpen() && getCurrentHandSum() < BLACKJACK;
    }

    private int valueFor(Card card) {
        if (card.isFaceCard()) {
            return FACE_CARD_VALUE;
        } else if (card.isAce()) {
            return 11;
        } else {
            return card.rank.numericValue();
        }
    }

    boolean hasBlackjack() {
        return currentHand.size() == 2 && getCurrentHandSum() == BLACKJACK;
    }

    void closeHand() {
        currentHand.close();
    }

    boolean isBusted() {
        return getCurrentHandSum() > BLACKJACK;
    }

    private String handSumString() {
        int sum = currentHandSum.get();
        String handSum = " => " + sum;
        if (hasBlackjack()) {
            handSum += " BLACKJACK";
        } else if (sum > BLACKJACK) {
            handSum += " BUSTED";
        }
        return handSum;
    }

    void clearHands() {
        hands.forEach(Hand::clear);
        if (hands.size() == 2) {
            hands.remove(1);
        }
        currentHand = hands.get(0);
        currentHand.open();
    }

    @Override
    public String toString() {
        return super.toString() + handSumString();
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

    void setMoney(int amount) {
        this.money.set(amount);
    }

    boolean canDoubleOrSplit(int bet) {
        return bet * 2 <= getMoney();
    }

    boolean canSplit(int bet) {
        ObservableList<Card> cards = currentHand.getCards();
        return cards.size() == 2 && canDoubleOrSplit(bet) && valueFor(cards.get(0)) == valueFor(cards.get(1));
    }
}
