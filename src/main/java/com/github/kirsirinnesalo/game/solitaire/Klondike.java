package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Pile;
import com.github.kirsirinnesalo.control.ShiftDown;
import com.github.kirsirinnesalo.control.ShiftRight;
import com.github.kirsirinnesalo.model.Card;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.kirsirinnesalo.control.ShiftDown.SHIFT_NONE;
import static com.github.kirsirinnesalo.model.Card.Rank.ACE;
import static com.github.kirsirinnesalo.model.Card.Rank.KING;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Klondike extends SolitaireApplication {

    private int foundationRow;
    private int tableauRow;
    private int column1;
    private int foundationColumn1;

    @Override
    public String getTitle() {
        return "Klondike";
    }

    @Override
    public Node createStock() {
        Pile stock = (Pile) super.createStock();
        foundationRow = (int) stock.getLayoutY();
        tableauRow = foundationRow + getCardHeight() + 30;
        column1 = (int) stock.getLayoutX();
        foundationColumn1 = column1 + 3 * nextColumn();
        stock.setShift(new ShiftRight(0));
        return stock;
    }

    @Override
    public List<Pile> createTableauPiles() {
        ArrayList<Pile> tableauPiles = getPiles("tableau", 7, column1, tableauRow);
        tableauPiles.forEach(tableauPile -> {
            tableauPile.setOnMouseClicked(getDoubleClickHandlerFor(tableauPile));
            tableauPile.getCards().addListener((ListChangeListener<CardView>) change -> {
                if (change.next() && !change.wasAdded() && change.wasRemoved()) {
                    tableauPile.getTopCardView().turnFaceUp();
                }
            });
        });
        return tableauPiles;
    }

    private EventHandler<MouseEvent> getDoubleClickHandlerFor(Pile pile) {
        return event -> {
            if (isDoubleClick(event)) {
                Card myCard = pile.getTopCard();
                getFoundationPiles().stream().filter(foundationPile ->
                        (myCard.isAce() && foundationPile.isEmpty()) ||
                                (foundationPile.getTopCard().suit == myCard.suit && foundationPile.getTopCard().rank == myCard.rank.previous())
                ).findFirst().ifPresent(foundationPile -> moveCard(pile, foundationPile));
            }
        };
    }

    @Override
    public List<Pile> createFoundationPiles() {
        ArrayList<Pile> piles = getPiles("foundation", 4, foundationColumn1, foundationRow);
        piles.forEach(pile -> {
            pile.getChildren().add(getFoundationMark());
            pile.setShift(SHIFT_NONE);
        });
        return piles;
    }

    private Node getFoundationMark() {
        Text text = new Text("A");
        text.setFont(Font.font(20));
        text.setFill(Color.GOLDENROD);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.TOP);
        text.setTranslateY(-2);
        text.setTranslateX(-1);
        Circle circle = new Circle(text.getLayoutBounds().getWidth() * 2 + 2);
        circle.setStroke(Color.GOLDENROD);
        circle.setFill(Color.TRANSPARENT);
        circle.setStrokeWidth(2);
        StackPane pane = new StackPane();
        pane.getChildren().addAll(circle, text);
        return pane;
    }

    @Override
    public List<Pile> createReservePiles() {
        return Collections.emptyList();
    }

    @Override
    public void deal() {
        setUpDiscardPile();

        List<Pile> piles = getTableauPiles();
        IntStream.rangeClosed(1, piles.size()).forEach(pileNumber -> {
            for (int numberOfCards = 1; numberOfCards <= pileNumber; numberOfCards++) {
                CardView card = getStock().giveCard();
                Pile pile = piles.get(pileNumber - 1);
                pile.addCard(card);
            }
        });
        piles.forEach(pile -> pile.getTopCardView().turnFaceUp());
    }

    @Override
    public void hit() {
        Pile stock = getStock();
        Pile discardPile = getDiscardPile();
        if (!stock.isEmpty()) {
            CardView card = stock.giveCard();
            card.turnFaceUp();
            card.setTranslateX(0);
            discardPile.addCard(card);
        } else if (!discardPile.isEmpty()) {
            new ArrayDeque<>(discardPile.getCards()).descendingIterator().forEachRemaining(card -> {
                card.setTranslateX(0);
                stock.addCard(card);
            });
            discardPile.clear();
            stock.getCards().forEach(CardView::turnFaceDown);
        }
    }

    @Override
    public EventHandler<DragEvent> getOnDragDroppedHandler() {
        return event -> {
            CardView cardView = (CardView) event.getGestureSource();
            Pile sourcePile = (Pile) cardView.getParent();
            Pile targetPile = (Pile) event.getGestureTarget();
            if (!sourcePile.equals(targetPile)) {
                boolean dropCard = false;
                Card card = cardView.getCard();
                if (isFoundationPile(targetPile) && sourcePile.getTopCardView().equals(cardView)) {
                    if (targetPile.isEmpty() && card.rank == ACE) {
                        dropCard = true;
                    } else {
                        Card topCard = targetPile.getTopCard();
                        if (card.suit == topCard.suit && topCard.isSmallerThan(KING) && card.rank == topCard.rank.next()) {
                            dropCard = true;
                        }
                    }
                } else if (isTableauPile(targetPile)) {
                    if (targetPile.isEmpty() && card.rank == KING) {
                        dropCard = true;
                    } else {
                        Card topCard = targetPile.getTopCard();
                        if (card.getColor() != topCard.getColor() && topCard.rank.previous() == card.rank) {
                            dropCard = true;
                        }
                    }
                }
                if (dropCard) {
                    if (!cardView.equals(sourcePile.getTopCardView())) {
                        ObservableList<CardView> cards = sourcePile.getCards();
                        int indexOfMyCard = cards.indexOf(cardView);
                        List<CardView> cardsToMove = cards.subList(indexOfMyCard, cards.size() - 1);
                        cardsToMove.forEach(c -> c.setTranslateX(0));
                        targetPile.addAll(cardsToMove);
                        cards.removeAll(cardsToMove);
                    }
                    moveCard(sourcePile, targetPile);
                    checkIfGameOver();
                }
            }
            event.consume();
        };
    }

    private void checkIfGameOver() {
        List<Pile> piles = Stream.of(getTableauPiles())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
        piles.addAll(asList(getStock(), getDiscardPile()));
        if (piles.stream().allMatch(Pile::isEmpty) && foundationPilesAreFull()) {
            gameWon();
        }
    }

    private boolean foundationPilesAreFull() {
        return getFoundationPiles().stream().allMatch(pile -> pile.getCards().size() == 13);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnDragDetectedHandler() {
        return event -> {
            CardView cardView = (CardView) event.getSource();
            ObservableList<CardView> cards = ((Pile) cardView.getParent()).getCards();
            if (!cards.get(cards.size() - 1).equals(cardView) && !cardView.isFaceUp()) {
                event.setDragDetect(false);
            } else {
                ClipboardContent content = new ClipboardContent();
                content.putString(cardView.toString());

                Dragboard dragboard = cardView.startDragAndDrop(TransferMode.MOVE);
                dragboard.setContent(content);

                ImageView cardSnapshot = new ImageView(cardView.snapshot(null, null));
                dragboard.setDragView(cardSnapshot.getImage(), event.getX(), event.getY());
            }
            event.consume();
        };
    }

    private void setUpDiscardPile() {
        Pile discardPile = getDiscardPile();
        discardPile.setLayoutX(column1 + nextColumn());
        discardPile.setLayoutY(foundationRow);
        discardPile.setVisible(true);
        discardPile.setShift(new ShiftRight(1));
        discardPile.setOnMouseClicked(getDoubleClickHandlerFor(discardPile));
    }

    private ArrayList<Pile> getPiles(String id, int numberOfPiles, int startColumn, int row) {
        ArrayList<Pile> piles = new ArrayList<>();
        AtomicInteger column = new AtomicInteger(startColumn);
        IntStream.rangeClosed(1, numberOfPiles).forEach(i -> {
            Pile pile = getPile(id + i, column, row);
            piles.add(pile);
            pile.setShift(new ShiftDown(15));
            column.addAndGet(nextColumn());
        });
        return piles;
    }

    private Pile getPile(String id, AtomicInteger column, int row) {
        Pile pile = createPile(id, column.get(), row);
        pile.setShift(SHIFT_NONE);
        makeDragTarget(pile);
        return pile;
    }

    private int nextColumn() {
        return getCardWidth() + 20;
    }

}
