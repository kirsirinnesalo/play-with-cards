package com.github.kirsirinnesalo.control;

public class ShiftRight extends StackShift {
    public ShiftRight(double translateAmount) {
        super(translateAmount);
    }

    @Override
    public void shift(CardView cardView) {
        cardView.setTranslateX(getTranslate());
    }
}
