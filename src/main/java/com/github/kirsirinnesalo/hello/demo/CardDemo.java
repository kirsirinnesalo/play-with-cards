package com.github.kirsirinnesalo.hello.demo;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.control.CardView;
import com.github.kirsirinnesalo.control.DraggableCardView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CardDemo extends Application {

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
