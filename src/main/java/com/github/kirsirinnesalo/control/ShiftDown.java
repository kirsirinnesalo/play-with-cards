package com.github.kirsirinnesalo.control;

public class ShiftDown extends PileShift {
    public ShiftDown(double translateAmount) {
        super(translateAmount);
    }

    @Override
    public void shift(CardView cardView) {
        cardView.setTranslateY(getTranslate());
    }
}
