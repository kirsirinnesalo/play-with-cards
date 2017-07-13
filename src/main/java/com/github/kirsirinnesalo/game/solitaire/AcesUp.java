package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Pile;
import com.github.kirsirinnesalo.control.ShiftDown;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AcesUp extends SolitaireApplication {

    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 400;

    @Override
    public String getTitle() {
        return "Aces Up";
    }

    @Override
    public double getWidth() {
        return TABLE_WIDTH;
    }

    @Override
    public double getHeight() {
        return TABLE_HEIGHT;
    }

    @Override
    public List<Pile> createTableauPiles() {
        List<Pile> piles = new ArrayList<>(4);
        piles.addAll(Stream.of(
                createPile("1", 155, 45),
                createPile("2", 255, 45),
                createPile("3", 355, 45),
                createPile("4", 455, 45))
                .collect(toList()));
        piles.forEach(pile -> {
            makeDragTarget(pile);
            pile.setShift(new ShiftDown(5));
            pile.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = pile.getTopCard();
                    if (existsPileTopGreaterThan(myTopCard)) {
                        getDiscardPile().addCard(pile.giveCard());
                    }
                }
                checkIfGameOver();
            });
        });
        return piles;
    }

    @Override
    public List<Pile> createFoundationPiles() {
        return Collections.emptyList();
    }

    @Override
    public List<Pile> createReservePiles() {
        return Collections.emptyList();
    }

    @Override
    public void deal() {
        //no initial deal on tableau
    }

    @Override
    public void hit() {
        getTableauPiles().forEach(pile -> {
            if (!getStock().isEmpty()) {
                CardView card = getStock().giveCard();
                card.turnFaceUp();
                pile.addCard(card);
            }
        });
        checkIfGameOver();
    }

    @Override
    public EventHandler<DragEvent> getOnDragDroppedHandler() {
        return event -> {
            CardView cardView = (CardView) event.getGestureSource();

            Pile sourcePile = (Pile) cardView.getParent();
            Pile targetPile = (Pile) event.getGestureTarget();

            if (targetPile.isEmpty()) {
                sourcePile.giveCard();
                targetPile.addCard(cardView);
            }
        };
    }

    private void checkIfGameOver() {
        if (getStock().isEmpty()) {
            if (getTableauPiles().stream().allMatch(this::pileOnlyCardIsAce)) {
                gameWon();
            } else if (!movesLeft()) {
                gameLost();
            }
        }
    }

    private boolean movesLeft() {
        return getTableauPiles().stream()
                .map(Pile::getTopCard)
                .anyMatch(this::existsPileTopGreaterThan) || canMoveToEmptyPile();
    }

    private boolean canMoveToEmptyPile() {
        return anyTableauPileIsEmpty() && anyTableauPilesHasMoreThanOneCards();
    }

    private boolean anyTableauPilesHasMoreThanOneCards() {
        return getTableauPiles().stream().anyMatch(pile -> pile.getCards().size() > 1);
    }

    private boolean anyTableauPileIsEmpty() {
        return getTableauPiles().stream().anyMatch(Pile::isEmpty);
    }

    private boolean pileOnlyCardIsAce(Pile pile) {
        List<CardView> cards = pile.getCards();
        return cards.size() == 1 && cards.get(0).getCard().isAce();
    }

    private boolean existsPileTopGreaterThan(Card myTopCard) {
        return getTableauPiles().stream()
                .filter(otherPile -> otherPile.getTopCard().suit.equals(myTopCard.suit))
                .anyMatch(otherPile -> {
                    Card otherCard = otherPile.getTopCard();
                    return !myTopCard.isAce() && !otherCard.rank.equals(Rank.JOKER) &&
                            (otherCard.isAce() || otherCard.rank.numericValue() > myTopCard.rank.numericValue());
                });
    }

}
