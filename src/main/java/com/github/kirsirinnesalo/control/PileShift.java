package com.github.kirsirinnesalo.control;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public abstract class PileShift {
    private double amount;
    private double translate;

    public PileShift(double translateAmount) {
        this.amount = translateAmount;
    }

    public double getTranslate() {
        return translate;
    }

    public abstract void shift(CardView cardView);

    void shiftOverlap(Pile pile, CardView cardView) {
        ObservableList<Node> cardList = pile.getChildren();
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

