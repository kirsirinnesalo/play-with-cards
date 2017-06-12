package com.github.kirsirinnesalo.scene.control;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/*https://stackoverflow.com/questions/14860960/groupbox-titledborder-in-javafx-2 */
public class BorderedTitledPane extends StackPane {
    public BorderedTitledPane(String titleText, Node content) {
        Label title = new Label(titleText);
        title.getStyleClass().add("bordered-titled-title");
        title.setStyle("-fx-background-color: lightgrey;"
                + " -fx-background-radius: 10 10 10 10;"
                + " -fx-border: thin solid black;"
                + " -fx-translate-y: -16;"
                + " -fx-z-index: 99;"
                + " -fx-padding: 5 10 5 10;");
        StackPane.setAlignment(title, Pos.TOP_CENTER);

        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("bordered-titled-content");
        contentPane.setStyle("-fx-padding: 25 10 10 10;");
        contentPane.getChildren().add(content);

        getStyleClass().add("bordered-titled-border");
        setStyle("-fx-content-display: top;"
                + " -fx-border-radius: 10 10 10 10;"
                + " -fx-background-radius: 10 10 10 10;"
                + " -fx-border-insets: 20 15 15 15;"
                + " -fx-background-color: white;"
                + " -fx-border-color: black;"
                + " -fx-border-width: 1;");

        getChildren().addAll(title, contentPane);
    }

}
