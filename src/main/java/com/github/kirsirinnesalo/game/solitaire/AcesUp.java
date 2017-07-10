package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.ShiftDown;
import com.github.kirsirinnesalo.control.Stack;
import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.model.Card.Rank;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AcesUp extends SolitaireGameApplication {

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
            stack.setShift(new ShiftDown(5));
            stack.setOnMouseClicked(event -> {
                if (isDoubleClick(event)) {
                    Card myTopCard = topCardIn(stack);
                    if (existsStackTopGreaterThan(myTopCard)) {
                        getDiscardPile().addCard(stack.giveCard());
                    }
                }
                checkIfGameOver();
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

            if (targetStack.isEmpty()) {
                sourceStack.giveCard();
                targetStack.addCard(cardView);
            }
        };
    }

    @Override
    public void deal() {
        getStacks().forEach(stack -> {
            if (!getDeck().isEmpty()) {
                CardView card = getDeck().giveCard();
                card.turnFaceUp();
                stack.addCard(card);
            }
        });
        checkIfGameOver();
    }

    @Override
    public void checkIfGameOver() {
        if (getDeck().isEmpty()) {
            if (getStacks().stream().allMatch(this::stackOnlyCardIsAce)) {
                gameWon();
            } else if (!movesLeft()) {
                gameLost();
            }
        }
    }

    private boolean movesLeft() {
        return getStacks().stream().anyMatch(stack -> existsStackTopGreaterThan(topCardIn(stack)))
                || (getStacks().stream().anyMatch(Stack::isEmpty)
                && getStacks().stream().anyMatch(stack -> stack.getCards().size() > 1));
    }

    private boolean stackOnlyCardIsAce(Stack stack) {
        List<CardView> cards = stack.getCards();
        return cards.size() == 1 && cards.get(0).getCard().isAce();
    }

    private boolean existsStackTopGreaterThan(Card myTopCard) {
        return getStacks().stream()
                .filter(otherStack -> topCardIn(otherStack).suit.equals(myTopCard.suit))
                .anyMatch(otherStack -> {
                    Card otherCard = topCardIn(otherStack);
                    return !myTopCard.isAce() && !otherCard.rank.equals(Rank.JOKER) &&
                            (otherCard.isAce() || otherCard.rank.numericValue() > myTopCard.rank.numericValue());
                });
    }

}
