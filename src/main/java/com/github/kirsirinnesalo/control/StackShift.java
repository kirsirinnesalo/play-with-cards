package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.Stack;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public abstract class StackShift {
    private double amount;
    private double translate;

    public StackShift(double translateAmount) {
        this.amount = translateAmount;
    }

    public double getTranslate() {
        return translate;
    }

    public abstract void shift(CardView cardView);

    public void shiftOverlap(Stack stack, CardView cardView) {
        ObservableList<Node> cardList = stack.getChildren();
        int cardCount = cardList.size();
        if (cardCount > 0) {
            if (cardCount > 1) {
                setTranslate((cardCount - 2) * amount + amount);
            } else {
                setTranslate(0);
            }
            shift(cardView);
        }
    }

    private void setTranslate(double translate) {
        this.translate = translate;
    }
}

