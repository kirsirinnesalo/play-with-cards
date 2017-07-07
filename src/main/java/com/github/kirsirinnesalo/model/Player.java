package com.github.kirsirinnesalo.model;

public class Player {
    private Hand hand;
    private String name;

    public Player(String name) {
        this.name = name;
        this.hand = new Hand();
    }

    public String getName() {
        return name;
    }

    public Hand getHand() {
        return hand;
    }

    public void takeCard(Card card) {
        getHand().takeCard(card);
    }

    @Override
    public String toString() {
        return getName() + ": " + getHand().handAsString();
    }

}
