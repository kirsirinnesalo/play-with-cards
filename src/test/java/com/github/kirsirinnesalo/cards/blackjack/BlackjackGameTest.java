package com.github.kirsirinnesalo.cards.blackjack;

import com.github.kirsirinnesalo.cards.Card;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static com.github.kirsirinnesalo.cards.blackjack.BlackjackPlayer.NOBODY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackjackGameTest {

    @Test
    void hitForPlayer() {
        BlackjackPlayer testPlayer = new BlackjackPlayer("Test player", 100);
        int initialHandSize = testPlayer.getHand().size();
        new BlackjackGame().hit(testPlayer);
        int afterDealHandSize = testPlayer.getHand().size();
        assertTrue(initialHandSize < afterDealHandSize,
                "Expected player card count bigger after deal. Was <" + afterDealHandSize + ">");

    }

    @Test
    void hitOneCard() {
        BlackjackPlayer player = new BlackjackPlayer("Test player", 100);
        final int initialCards = player.getHand().size();
        new BlackjackGame().hit(player);
        int afterDealCards = player.getHand().size();
        assertEquals(initialCards + 1, afterDealCards);
    }

    @Test
    void resolveWinnerWhenPlayerWins() {
        BlackjackPlayer player = getPlayerWithSum(20);
        BlackjackGame game = createGame(getDealerWithSum(17), player);
        assertEquals(player, game.resolveWinner());
    }

    @Test
    void resolveWinnerWhenDealerWins() {
        final BlackjackDealer dealer = getDealerWithSum(20);
        BlackjackGame game = createGame(dealer, getPlayerWithSum(17));
        assertEquals(dealer, game.resolveWinner());
    }

    @Test
    void resolveWinnerOnPush() {
        final int pushSum = 20;
        BlackjackGame pushGame = createGame(getDealerWithSum(pushSum), getPlayerWithSum(pushSum));
        assertEquals(NOBODY, pushGame.resolveWinner());
    }

    @Test
    void pushWhenBothBusted() {
        final int bustSum = 27;
        BlackjackGame game = createGame(getDealerWithSum(bustSum), getPlayerWithSum(bustSum));
        assertEquals(NOBODY, game.resolveWinner());
    }

    @Test
    void resolveWinnerWhenPlayerBusted() {
        BlackjackDealer dealer = getDealerWithSum(20);
        BlackjackGame game = createGame(dealer, getPlayerWithSum(27));
        assertEquals(dealer, game.resolveWinner());
    }

    @Test
    void resolveWinnerWhenDealerBusted() {
        BlackjackPlayer player = getPlayerWithSum(20);
        BlackjackGame game = createGame(getDealerWithSum(27), player);
        assertEquals(player, game.resolveWinner());
    }

    @Test
    void playerWinsBetWhenWinsTheGame() {
        BlackjackPlayer player = getPlayerWithSum(20);
        BlackjackDealer dealer = getDealerWithSum(19);
        int money = player.getMoney();
        int bet = 10;
        playGame(player, dealer, bet, player);
        assertEquals(money + bet, player.getMoney());
    }

    @Test
    void bustedPlayerLosesTheBetWhenDealerWins() {
        BlackjackPlayer busted = getPlayerWithSum(22);
        BlackjackDealer dealer = getDealerWithSum(20);
        int money = busted.getMoney();
        int bet = 10;
        playGame(busted, dealer, bet, dealer);
        assertEquals(money - bet, busted.getMoney());
    }

    @Test
    void bustedPlayerKeepsTheBetWhenDealerBusted() {
        BlackjackPlayer player = getPlayerWithSum(22);
        int money = player.getMoney();
        playGame(player, getDealerWithSum(23), 10, NOBODY);
        assertEquals(money, player.getMoney());
    }

    @Test
    void blackjackPaysOneAndHalfOfBet() {
        BlackjackPlayer player = getPlayerWithBlackjack();
        int money = player.getMoney();
        int bet = 10;
        playGame(player, getDealerWithSum(18), bet, player);
        assertEquals(money + bet * 1.5, player.getMoney());
    }

    private void playGame(BlackjackPlayer player, BlackjackDealer dealer, int bet, BlackjackPlayer winner) {
        BlackjackGame game = createGame(dealer, player);
        game.setBet(bet);
        player.quitGame();
        game.payBet(winner);
    }

    private BlackjackDealer getDealerWithSum(final int sum) {
        return new BlackjackDealer("Test dealer", sum) {
            @Override
            int countSum() {
                return sum;
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

    private BlackjackPlayer getPlayerWithBlackjack() {
        return new BlackjackPlayer("Test player", 100) {
            @Override
            public ObservableList<Card> getHand() {
                Card king = new Card(Card.Rank.KING, Card.Suit.HEARTS);
                Card ace = new Card(Card.Rank.ACE, Card.Suit.HEARTS);
                return FXCollections.observableArrayList(king, ace);
            }
        };
    }

    private BlackjackGame createGame(BlackjackDealer gameDealer, BlackjackPlayer gamePlayer) {
        return new BlackjackGame() {
            @Override
            BlackjackPlayer getPlayer() {
                return gamePlayer;
            }

            @Override
            BlackjackDealer getDealer() {
                return gameDealer;
            }
        };
    }

}