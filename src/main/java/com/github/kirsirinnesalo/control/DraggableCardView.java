package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.model.Card;

import javafx.scene.Cursor;

public class DraggableCardView extends CardView {
    private final Delta delta = new Delta();

    public DraggableCardView(Card card, int cardWidth, int cardHeight) {
        super(card, cardWidth, cardHeight);

        setOnMouseEntered(e -> setCursor(Cursor.HAND));
        setOnMousePressed(event -> {
            setCursor(Cursor.CLOSED_HAND);
            delta.x = getTranslateX() - event.getSceneX();
            delta.y = getTranslateY() - event.getSceneY();
        });
        setOnMouseDragged(event -> {
            setCursor(Cursor.CLOSED_HAND);
            setTranslateX(event.getSceneX() + delta.x);
            setTranslateY(event.getSceneY() + delta.y);
        });
        setOnMouseReleased(e -> setCursor(Cursor.HAND));
    }

    private class Delta {
        double x, y;
    }

}
