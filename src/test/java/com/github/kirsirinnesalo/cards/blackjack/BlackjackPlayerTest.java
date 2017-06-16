package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Card;
import com.github.kirsirinnesalo.cards.Card.Rank;
import com.github.kirsirinnesalo.cards.Card.Suit;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static org.junit.jupiter.api.Assertions.*;

class BlackjackPlayerTest {
    @Test
    void whenBlackjackPlayerDoesNotNeedMoreCards() {
        assertFalse(getPlayerWithSum(BlackjackGame.BLACKJACK).needsMoreCards());
    }

    @Test
    void blackjackIsFromFirstDeal() {
        Card face = new Card(Rank.KING, Suit.HEARTS);
        Card ace = new Card(Rank.ACE, Suit.SPADES);
        Card nine = new Card(Rank.NINE, Suit.HEARTS);

        BlackjackPlayer twoCardPlayer = getPlayerWithHand(face, ace);
        assertEquals(true, twoCardPlayer.hasBlackjack());

        BlackjackPlayer threeCardPlayer = getPlayerWithHand(nine, ace, ace);
        assertEquals(false, threeCardPlayer.hasBlackjack());
    }

    @Test
    void aceIsCountedToEleven() {
        Card ace = new Card(Rank.ACE, Suit.HEARTS);
        Card nine = new Card(Rank.NINE, Suit.HEARTS);
        assertEquals(20, getPlayerWithHand(ace, nine).countSum());
    }

    @Test
    void aceIsCountedToOne() {
        Card ace = new Card(Rank.ACE, Suit.HEARTS);
        Card nine = new Card(Rank.NINE, Suit.HEARTS);
        Card deuce = new Card(Rank.DEUCE, Suit.HEARTS);
        assertEquals(12, getPlayerWithHand(ace, nine, deuce).countSum());
    }

    private BlackjackPlayer getPlayerWithHand(Card... cards) {
        return new BlackjackPlayer("Test player", 100) {
            @Override
            public ObservableList<Card> getHand() {
                return FXCollections.observableArrayList(cards);
            }
        };
    }

    private BlackjackPlayer getPlayerWithSum(final int sum) {
        return new BlackjackPlayer("Test player", 100) {
            @Override
            int countSum() {
                return sum;
            }
        };
    }
}