package com.github.kirsirinnesalo.game.blackjack;


class BlackjackDealer extends BlackjackPlayer {
    private final int playUntilSum;

    BlackjackDealer(String name, int playUntilSum) {
        super(name, 0);
        this.playUntilSum = playUntilSum;
    }

    @Override
    public boolean needsMoreCards() {
        return getCurrentHandSum() < playUntilSum;
    }

}
