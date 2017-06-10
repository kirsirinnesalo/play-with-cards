package com.github.kirsirinnesalo.cards;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Card {
    private static Collection<Rank> faceCards = Stream.of(Rank.JACK, Rank.QUEEN, Rank.KING).collect(Collectors.toList());

    public final Rank rank;
    public final Suit suit;

    private boolean faceUp;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.faceUp = true;
    }

    static boolean isFaceCard(Rank rank) {
        return faceCards.contains(rank);
    }

    static boolean isAce(Rank rank) {
        return Rank.ACE.equals(rank);
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void turnFaceDown() {
        faceUp = false;
    }

    public void turnFaceUp() {
        faceUp = true;
    }

    @Override
    public String toString() {
        return rank.value + suit.value;
    }

    public boolean isFaceCard() {
        return isFaceCard(rank);
    }

    public boolean isAce() {
        return isAce(rank);
    }

    public enum Color {BLACK, RED}

    //https://en.wikipedia.org/wiki/Playing_cards_in_Unicode
    public enum Suit {
        SPADES("\u2660", Color.BLACK),
        HEARTS("\u2665", Color.RED),
        DIAMONDS("\u2666", Color.RED),
        CLUBS("\u2663", Color.BLACK);

        final String value;
        final Color color;

        Suit(String value, Color color) {
            this.value = value;
            this.color = color;
        }

        public String asText() {
            return name().toLowerCase();
        }
    }

    public enum Rank {
        ACE("A"),
        DEUCE("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("10"),
        JACK("J"),
        QUEEN("Q"),
        KING("K");

        String value;

        Rank(String value) {
            this.value = value;
        }

        public int numericValue() {
            if (isFaceCard(this)) {
                return 0;
            } else if (isAce(this)) {
                return 1;
            }
            return Integer.parseInt(value);
        }

        public String asText() {
            String text = value;
            if (isAce(this)) {
                text = "ace";
            } else if (isFaceCard(this)) {
                text = name().toLowerCase();
            }
            return text;
        }
    }

}
