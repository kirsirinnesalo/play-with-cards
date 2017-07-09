package com.github.kirsirinnesalo.game.solitaire;

import com.github.kirsirinnesalo.control.CardView;

public class ShiftRight extends StackShift {
    public ShiftRight(double translateAmount) {
        super(translateAmount);
    }

    @Override
    public void shift(CardView cardView) {
        cardView.setTranslateX(getTranslate());
    }
}
