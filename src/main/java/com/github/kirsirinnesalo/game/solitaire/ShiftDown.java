package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;

public class ShiftDown extends StackShift {
    public ShiftDown(double translateAmount) {
        super(translateAmount);
    }

    @Override
    public void shift(CardView cardView) {
        cardView.setTranslateY(getTranslate());
    }
}
