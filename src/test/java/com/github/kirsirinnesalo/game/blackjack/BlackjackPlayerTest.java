package com.github.kirsirinnesalo.game.blackjack;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;
import com.github.kirsirinnesalo.model.Card.Suit;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BlackjackPlayerTest {
    @Test
    void whenBlackjackSumPlayerDoesNotNeedMoreCards() {
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
        assertEquals(20, getPlayerWithHand(ace, nine).getCurrentHandSum());
    }

    @Test
    void aceIsCountedToOne() {
        Card ace = new Card(Rank.ACE, Suit.HEARTS);
        Card nine = new Card(Rank.NINE, Suit.HEARTS);
        Card deuce = new Card(Rank.DEUCE, Suit.HEARTS);
        assertEquals(12, getPlayerWithHand(ace, nine, deuce).getCurrentHandSum());
    }

    @Test
    void countBustedSumWithAces() {
        Card aceOfHearts = new Card(Rank.ACE, Suit.HEARTS);
        Card aceOfClubs = new Card(Rank.ACE, Suit.CLUBS);
        Card deuce = new Card(Rank.DEUCE, Suit.DIAMONDS);
        Card kingOfHearts = new Card(Rank.KING, Suit.HEARTS);
        Card kingOfSpades = new Card(Rank.KING, Suit.SPADES);
        assertEquals(24, getPlayerWithHand(aceOfHearts, aceOfClubs, deuce, kingOfHearts, kingOfSpades).getCurrentHandSum());
    }

    private BlackjackPlayer getPlayerWithHand(Card... cards) {
        BlackjackPlayer player = new BlackjackPlayer("Test player", 100);
        Arrays.stream(cards).forEach(player::takeCard);
        return player;
    }

    private BlackjackPlayer getPlayerWithSum(final int sum) {
        return new BlackjackPlayer("Test player", 100) {
            @Override
            int getCurrentHandSum() {
                return sum;
            }
        };
    }
}