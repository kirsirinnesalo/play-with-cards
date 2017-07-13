package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.*;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.kirsirinnesalo.game.solitaire.COLUMN.*;
import static com.github.kirsirinnesalo.game.solitaire.ROW.*;
import static com.github.kirsirinnesalo.model.Card.Rank.*;
import static java.util.stream.Collectors.toList;

enum COLUMN {
    COLUMN_1(30),
    COLUMN_2(140),
    COLUMN_3(250),
    COLUMN_4(380);
    int x;

    COLUMN(int x) {
        this.x = x;
    }
}

enum ROW {
    ROW_1(50),
    ROW_2(190),
    ROW_3(330);
    int y;

    ROW(int y) {
        this.y = y;
    }
}

//FIXME waste shift pielessä kolmannella kierroksella

//TODO jakopakka käännetään vain kaksi kertaa
public class NapoleonsTomb extends SolitaireApplication {
    private static final ShiftDown SHIFT_NONE = new ShiftDown(0);
    private static final PileShift ShiftThreeRight = new PileShift(5) {
        int cards = 1;

        @Override
        public void shift(CardView cardView) {
            double translate;
            switch (cards) {
                case 1:
                    translate = 0;
                    cards = 2;
                    break;
                case 2:
                    translate = 5;
                    cards = 3;
                    break;
                case 3:
                    translate = 10;
                    cards = 1;
                    break;
                default:
                    translate = 0;
                    cards = 1;
            }
            cardView.setTranslateX(translate);
        }
    };
    private Pile wastePile;

    @Override
    public String getTitle() {
        return "Napoleonin hauta";
    }

    @Override
    public double getWidth() {
        return 500;
    }

    @Override
    public double getHeight() {
        return 500;
    }

    @Override
    public Node createStock() {
        Pile stock = (Pile) super.createStock();
        stock.setLayoutX(COLUMN_4.x);
        stock.setLayoutY(ROW_3.y);
        stock.setShift(SHIFT_NONE);
        Text label = getPileLabel("O");
        label.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 40));
        label.setFill(Color.GREEN);
        stock.getChildren().add(label);
        return stock;
    }

    @Override
    public List<Pile> createTableauPiles() {
        ArrayList<Pile> piles = new ArrayList<>(Arrays.asList(
                getPile("tableau-up", COLUMN_2, ROW_1),
                getPile("tableau-left", COLUMN_1, ROW_2),
                getPile("tableau-right", COLUMN_3, ROW_2),
                getPile("tableau-down", COLUMN_2, ROW_3)
        ));
        piles.forEach(this::makeDragTarget);
        return piles;
    }

    @Override
    public List<Pile> createFoundationPiles() {
        ArrayList<Pile> piles = new ArrayList<>(Arrays.asList(
                getPile("foundation-upleft", COLUMN_1, ROW_1),
                getPile("foundation-upright", COLUMN_3, ROW_1),
                getPile("foundation-downleft", COLUMN_1, ROW_3),
                getPile("foundation-downright", COLUMN_3, ROW_3),
                getPile("foundation-center", COLUMN_2, ROW_2)
        ));
        piles.forEach(pile -> {
            makeDragTarget(pile);
            Text text = getPileLabel("7 - K");
            if (isCenterPile(pile)) {
                text.setText("6 - A");
            }
            pile.getChildren().add(text);
        });
        return piles;
    }

    @Override
    public List<Pile> createReservePiles() {
        wastePile = createPile("reserve-stock", COLUMN_4.x, ROW_2.y);
        wastePile.setShift(ShiftThreeRight);
        Pile sixPile = getPile("reserve-six", COLUMN_4, ROW_1);
        sixPile.setShift(new ShiftRight(1));
        sixPile.getChildren().add(getPileLabel("6"));
        return new ArrayList<>(Arrays.asList(sixPile, wastePile));
    }

    private Text getPileLabel(String text) {
        Text label = new Text(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, FontPosture.ITALIC, 14));
        return label;
    }

    @Override
    public void deal() {
        getTableauPiles().forEach(pile -> getCardFromStock().ifPresent(pile::addCard));
    }

    @Override
    public void hit() {
        Pile stock = getStock();
        if (stock.isEmpty() && !wastePile.isEmpty()) {
            new ArrayDeque<>(wastePile.getCards())
                    .descendingIterator().forEachRemaining(stock::addCard);
            stock.getCards().forEach(card -> {
                card.turnFaceDown();
                card.setTranslateX(0);
            });
            wastePile.clear();
        } else {
            wastePile.addAll(get3CardsFromStock());
        }
    }

    @Override
    public EventHandler<DragEvent> getOnDragDroppedHandler() {
        return event -> {
            Pile sourcePile = (Pile) ((CardView) event.getGestureSource()).getParent();
            Pile targetPile = (Pile) event.getGestureTarget();

            if (isTableauPile(targetPile) && targetPile.isEmpty() && isWastePile(sourcePile)) {
                moveCard(sourcePile, targetPile);
            } else if (isSixPile(targetPile)) {
                if (sourcePile.getTopCard().rank == SIX) {
                    moveCard(sourcePile, targetPile);
                }
            } else {
                getAcceptRankFor(targetPile).ifPresent(acceptRank -> {
                    if (acceptRank == sourcePile.getTopCard().rank) {
                        moveCard(sourcePile, targetPile);
                    }
                });
                if ((isCornerPile(targetPile) && cornerPileIsFull(targetPile))
                        || (isSixPile(targetPile) && centerPileIsFull(targetPile))) {
                    targetPile.setDisable(true);
                }
            }
            checkIfGameOver();
        };
    }

    private boolean isWastePile(Pile pile) {
        return pile.getId().equals("reserve-stock");
    }

    private void moveCard(Pile sourcePile, Pile targetPile) {
        CardView card = sourcePile.giveCard();
        card.setTranslateX(0);
        targetPile.addCard(card);
    }

    private Pile getPile(String id, COLUMN column, ROW row) {
        Pile pile = createPile(id, column.x, row.y);
        pile.setShift(new ShiftDown(0));
        makeDragTarget(pile);
        return pile;
    }

    private Optional<Rank> getAcceptRankFor(Pile pile) {
        Rank acceptRank = null;
        Card topCard = pile.getTopCard();
        if (isCornerPile(pile)) {
            acceptRank = SEVEN;
            if (!pile.isEmpty() && topCard.isSmallerThan(KING)) {
                acceptRank = topCard.rank.next();
            } else if (topCard.rank == KING) {
                acceptRank = null;
            }
        } else if (isCenterPile(pile)) {
            acceptRank = SIX;
            if (!pile.isEmpty() && topCard.isGreaterThan(ACE)) {
                acceptRank = topCard.rank.previous();
            }
        } else if (isSixPile(pile) && pile.isEmpty()) {
            acceptRank = SIX;
        }
        return Optional.ofNullable(acceptRank);
    }

    private boolean isTableauPile(Pile pile) {
        return pile.getId().startsWith("tableau-");
    }

    private boolean isSixPile(Pile pile) {
        return pile.getId().equals("reserve-six");
    }

    private boolean isCornerPile(Pile pile) {
        return isFoundationPile(pile) && !isCenterPile(pile);
    }

    private boolean isCenterPile(Pile pile) {
        return pile.getId().equals("foundation-center");
    }

    private boolean isFoundationPile(Pile pile) {
        return getFoundationPiles().contains(pile);
    }

    private void checkIfGameOver() {
        List<Pile> piles = Stream.of(getTableauPiles(), getReservePiles())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
        piles.add(getStock());
        if (piles.stream().allMatch(Pile::isEmpty) && foundationPilesAreFull()) {
            gameWon();
        }
    }

    private boolean foundationPilesAreFull() {
        return getFoundationPiles().stream()
                .allMatch(pile -> centerPileIsFull(pile) || cornerPileIsFull(pile));
    }

    private boolean cornerPileIsFull(Pile pile) {
        return isCornerPile(pile) && pile.getCards().size() == 7 && pile.getTopCard().rank == KING;
    }

    private boolean centerPileIsFull(Pile pile) {
        return isCenterPile(pile) && pile.getCards().size() == 24 && pile.getTopCard().rank == ACE;
    }

    private List<CardView> get3CardsFromStock() {
        List<CardView> cards = new ArrayList<>(3);
        IntStream.rangeClosed(1, 3).forEach(i -> {
            Optional<CardView> card = getCardFromStock();
            card.ifPresent(cards::add);
        });
        return cards;
    }

    private Optional<CardView> getCardFromStock() {
        Optional<CardView> card = Optional.ofNullable(getStock().giveCard());
        card.ifPresent(CardView::turnFaceUp);
        return card;
    }

}
