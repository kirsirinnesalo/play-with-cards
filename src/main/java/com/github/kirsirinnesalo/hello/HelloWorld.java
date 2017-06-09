package com.github.kirsirinnesalo.hello;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloWorld extends Application {

    private static final String BUTTON_STYLE = "-fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );";
    private static final String HELLO_TEXT_STYLE
            = "-fx-font: 50px Tahoma;"
            + " -fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);"
            + " -fx-stroke: black; -fx-stroke-width: 1;";
    private static final Background TRANSPARENT_BACKGROUND
            = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

    public static void main(String[] args) {
        HelloWorld.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Hello World!");
        stage.setScene(createSceneWith(createPane()));
        stage.show();
    }

    private Scene createSceneWith(Pane pane) {
        return new Scene(pane, 300, 200, Color.MOCCASIN);
    }

    private BorderPane createPane() {
        BorderPane pane = new BorderPane();
        pane.setBackground(TRANSPARENT_BACKGROUND);

        addComponentsTo(pane);
        return pane;
    }

    private void addComponentsTo(BorderPane pane) {
        Text text = new Text();
        text.setStyle(HELLO_TEXT_STYLE);

        Button helloButton = createButton("Say hello...");
        doGreet(text, helloButton);

        Button exitButton = createButton("Quit");
        exitButton.setOnAction(event -> Platform.exit());

        addButtonsTo(pane, helloButton, exitButton);
        pane.setCenter(text);
    }

    private void addButtonsTo(BorderPane pane, Button... buttons) {
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.TOP_CENTER);
        buttonBox.setSpacing(50);
        buttonBox.setPadding(new Insets(20));
        buttonBox.getChildren().addAll(buttons);
        pane.setTop(buttonBox);
    }

    private void doGreet(Text text, Button helloButton) {
        helloButton.setOnAction(event -> {
            if (text.getText().isEmpty()) {
                text.setText("Hello World!\n");
                helloButton.setText("Take it back");
            } else {
                text.setText("");
                helloButton.setText("Say hello...");
            }
        });
    }

    private Button createButton(String label) {
        Button helloButton = new Button(label);
        helloButton.setStyle(BUTTON_STYLE);
        return helloButton;
    }
}
