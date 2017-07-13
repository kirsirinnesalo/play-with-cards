package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.ShiftDown;
import com.github.kirsirinnesalo.control.Pile;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Eternity extends SolitaireApplication {

    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 400;

    private int cardsInGame;

    @Override
    public String getTitle() {
        return "Ikuisuus";
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
    public Node createStock() {
        Node deck = super.createStock();
        cardsInGame = getStock().getCards().size();
        return deck;
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
            pile.setShift(new ShiftDown(2));
            pile.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = pile.getTopCard();
                    if (everyPileTopEquals(myTopCard.rank)) {
                        piles.forEach(s -> getDiscardPile().addCard(s.giveCard()));
                        if (getDiscardPile().getCards().size() == cardsInGame) {
                            gameWon();
                        }
                    }
                }
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
        ObservableList<CardView> stockCards = getStock().getCards();
        if (stockCards.size() >= getTableauPiles().size()) {
            getTableauPiles().forEach(pile -> {
                CardView card = getStock().giveCard();
                card.turnFaceUp();
                pile.addCard(card);
            });
        } else if (cardsLeftInTableau()) {
            collectCardsFromTableauToStockInReverseOrder();
            stockCards.forEach(CardView::turnFaceDown);
        }
    }

    @Override
    public EventHandler<DragEvent> getOnDragDroppedHandler() {
        return event -> {
            CardView cardView = (CardView) event.getGestureSource();

            Pile sourcePile = (Pile) cardView.getParent();
            Pile targetPile = (Pile) event.getGestureTarget();

            if (isTargetPileOnLeft(sourcePile, targetPile) && topCardRankEqual(targetPile, cardView)) {
                sourcePile.giveCard();
                targetPile.addCard(cardView);
            }
        };
    }

    private boolean cardsLeftInTableau() {
        return getTableauPiles().stream().anyMatch(pile -> !pile.isEmpty());
    }

    private void collectCardsFromTableauToStockInReverseOrder() {
        new ArrayDeque<>(getTableauPiles())
                .descendingIterator()
                .forEachRemaining(pile -> {
                    new ArrayDeque<>(pile.getCards())
                            .descendingIterator()
                            .forEachRemaining(card -> getStock().addCard(card));
                    pile.clear();
                });
    }

    private boolean topCardRankEqual(Pile targetPile, CardView cardView) {
        return targetPile.getTopCard().rank.equals(cardView.getCard().rank);
    }

    private boolean isTargetPileOnLeft(Pile sourcePile, Pile targetPile) {
        return Integer.valueOf(sourcePile.getId()) > Integer.valueOf(targetPile.getId());
    }

    private boolean everyPileTopEquals(Rank myTopCardRank) {
        return getTableauPiles().stream().allMatch(otherPile -> otherPile.getTopCard().rank.equals(myTopCardRank));
    }

}
