package com.github.kirsirinnesalo.hello.demo;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.control.CardView;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DraggableCardDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Card Demo");
        stage.setScene(createScene());
        stage.show();
    }

    private Scene createScene() {
        StackPane table = new StackPane();
        table.getChildren().add(createCard());
        return new Scene(table, 600, 600);
    }

    private CardView createCard() {
        Card queenOfHearts = new Card(Card.Rank.QUEEN, Card.Suit.HEARTS);
        CardView cardView = new DraggableCardView(queenOfHearts, 300, 400);
        cardView.setOnMouseClicked(event -> cardView.turnFace());
        return cardView;
    }

}

class DraggableCardView extends CardView {
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

