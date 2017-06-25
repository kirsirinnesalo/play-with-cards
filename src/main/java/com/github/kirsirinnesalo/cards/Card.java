package com.github.kirsirinnesalo.cards;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Card implements Comparable<Card> {
    private static Collection<Rank> faceCards = Stream.of(Rank.JACK, Rank.QUEEN, Rank.KING).collect(Collectors.toList());

    public final Rank rank;
    public final Suit suit;
    private final Color color;

    public Card(Rank rank, Suit suit) {
        this(rank, suit, suit.color);
    }

    public Card(Rank rank, Suit suit, Color color) {
        this.rank = rank;
        this.suit = suit;
        this.color = color;
    }

    static boolean isFaceCard(Rank rank) {
        return faceCards.contains(rank);
    }

    static boolean isAce(Rank rank) {
        return Rank.ACE.equals(rank);
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

    @Override
    public int compareTo(Card otherCard) {
        return rank.compareTo(otherCard.rank);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Card card = (Card) other;

        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        int result = rank != null ? rank.hashCode() : 0;
        result = 31 * result + (suit != null ? suit.hashCode() : 0);
        return result;
    }

    public Color getColor() {
        return color;
    }

    public enum Color {BLACK, RED}

    //https://en.wikipedia.org/wiki/Playing_cards_in_Unicode
    public enum Suit {
        SPADES("\u2660", Color.BLACK),
        HEARTS("\u2665", Color.RED),
        DIAMONDS("\u2666", Color.RED),
        CLUBS("\u2663", Color.BLACK),
        JOKER("JOKER", Color.RED);

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
        KING("K"),
        JOKER("");

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
