package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.ShiftDown;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Eternity extends SolitaireGameApplication {

    private static final int TABLE_WIDTH = 600;
    private static final int TABLE_HEIGHT = 400;

    private int cardsInGame;

    @Override
    public String getTitle() {
        return "Ikuisuus / Eternity";
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
    public Node createDeck() {
        Node deck = super.createDeck();
        cardsInGame = getDeck().getCards().size();
        return deck;
    }

    @Override
    public List<Stack> createStacks() {
        List<Stack> stacks = new ArrayList<>(4);
        stacks.addAll(Stream.of(
                createStack("1", 155, 45),
                createStack("2", 255, 45),
                createStack("3", 355, 45),
                createStack("4", 455, 45))
                .collect(toList()));
        stacks.forEach(stack -> {
            makeDragTarget(stack);
            stack.setShift(new ShiftDown(2));
            stack.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = topCardIn(stack);
                    if (everyStackTopEquals(myTopCard.rank)) {
                        stacks.forEach(s -> getDiscardPile().addCard(s.giveCard()));
                        checkIfGameOver();
                    }
                }
            });
        });
        return stacks;
    }

    @Override
    public EventHandler<DragEvent> getOnDragDroppedHandler() {
        return event -> {
            CardView cardView = (CardView) event.getGestureSource();

            Stack sourceStack = (Stack) cardView.getParent();
            Stack targetStack = (Stack) event.getGestureTarget();

            if (isTargetStackOnLeft(sourceStack, targetStack) && topCardRankEqual(targetStack, cardView)) {
                sourceStack.giveCard();
                targetStack.addCard(cardView);
            }
        };
    }

    @Override
    public void deal() {
        if (getDeck().getCards().size() >= getStacks().size()) {
            getStacks().forEach(stack -> {
                CardView card = getDeck().giveCard();
                card.turnFaceUp();
                stack.addCard(card);
            });
        } else if (getStacks().stream().anyMatch(stack -> !stack.isEmpty())) {
            new ArrayDeque<>(getStacks())
                    .descendingIterator()
                    .forEachRemaining(stack -> {
                        new ArrayDeque<>(stack.getCards())
                                .descendingIterator()
                                .forEachRemaining(card -> getDeck().addCard(card));
                        stack.clear();
                    });
            getDeck().getCards().forEach(CardView::turnFaceDown);
        }
    }

    @Override
    public void checkIfGameOver() {
        if (getDiscardPile().getCards().size() == cardsInGame) {
            gameWon();
        }
    }

    private boolean topCardRankEqual(Stack targetStack, CardView cardView) {
        return topCardIn(targetStack).rank.equals(cardView.getCard().rank);
    }

    private boolean isTargetStackOnLeft(Stack sourceStack, Stack targetStack) {
        return Integer.valueOf(sourceStack.getId()) > Integer.valueOf(targetStack.getId());
    }

    private boolean everyStackTopEquals(Rank myTopCardRank) {
        return getStacks().stream().allMatch(otherStack -> topCardIn(otherStack).rank.equals(myTopCardRank));
    }

}
