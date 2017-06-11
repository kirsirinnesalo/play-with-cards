package com.github.kirsirinnesalo.hello;

import com.github.kirsirinnesalo.scene.control.ColorSelector;
import com.github.kirsirinnesalo.scene.control.FontSelector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;

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
        return new Scene(pane, 800, 400);
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

        Label headerLabel = new Label("Hey!");

        addTopTo(pane, headerLabel, helloButton, exitButton);
        pane.setCenter(text);
        pane.setLeft(new FontSelector(headerLabel) {{
            getSelectionModel().select("Arial Black");
        }});
        pane.setRight(new ColorSelector(pane) {{
            getSelectionModel().select("Moccasin");
        }});
    }

    private void addTopTo(BorderPane pane, Control... nodes) {
        GridPane box = new GridPane();
        box.setAlignment(Pos.TOP_CENTER);
        range(0, nodes.length).forEach($ -> {
            box.getColumnConstraints().add(new ColumnConstraints() {{
                setHgrow(Priority.ALWAYS);
                setFillWidth(true);
                setPercentWidth(100 / nodes.length);
            }});
        });
        box.setPadding(new Insets(20));

        AtomicInteger column = new AtomicInteger(0);
        asList(nodes).forEach(node -> {
            box.add(node, column.getAndAdd(1), 0);
            GridPane.setHalignment(node, HPos.CENTER);
        });

        pane.setTop(box);
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
