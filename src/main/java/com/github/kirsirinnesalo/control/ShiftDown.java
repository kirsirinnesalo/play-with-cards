package com.github.kirsirinnesalo.control;

public class ShiftDown extends PileShift {
    public static final ShiftDown SHIFT_NONE = new ShiftDown(0);

    public ShiftDown(double translateAmount) {
        super(translateAmount);
    }

    @Override
    public void shift(CardView cardView) {
        cardView.setTranslateY(getTranslate());
    }
}
