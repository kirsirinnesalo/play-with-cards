package com.github.kirsirinnesalo.cards;

import com.github.kirsirinnesalo.cards.Card.Rank;
import com.github.kirsirinnesalo.cards.Card.Suit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Deck52 extends Deck {

    @Override
    public ObservableList<Card> setupDeck() {
        return Arrays.stream(Suit.values())
                .flatMap(suit -> Arrays.stream(Rank.values())
                        .map(rank -> new Card(rank, suit)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

}
