package com.github.kirsirinnesalo.cards.blackjack;


class BlackjackDealer extends BlackjackPlayer {
    private final int playUntilSum;

    BlackjackDealer(String name, int playUntilSum) {
        super(name, 0);
        this.playUntilSum = playUntilSum;
    }

    @Override
    public boolean needsMoreCards() {
        return countSum() < playUntilSum;
    }

}
