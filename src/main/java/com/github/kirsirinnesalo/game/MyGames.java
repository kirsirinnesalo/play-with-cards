package com.github.kirsirinnesalo.game;

import com.github.kirsirinnesalo.game.blackjack.BlackjackGUI;
import com.github.kirsirinnesalo.game.solitaire.AcesUp;
import com.github.kirsirinnesalo.game.solitaire.Eternity;
import com.github.kirsirinnesalo.game.solitaire.NapoleonsTomb;
import com.github.kirsirinnesalo.game.wof.WheelOfFortune;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class MyGames extends FXGameApplication {
    private static List<Class<? extends FXGameApplication>> solitaires = new ArrayList<>();
    private static List<Class<? extends FXGameApplication>> otherGames = new ArrayList<>();

    static {
        solitaires.addAll(Arrays.asList(
                AcesUp.class,
                Eternity.class,
                NapoleonsTomb.class
        ));
        otherGames.addAll(Arrays.asList(
                BlackjackGUI.class,
                WheelOfFortune.class
        ));
    }

    public static void main(String[] args) {
        MyGames.launch(args);
    }

    @Override
    public String getTitle() {
        return "Pelejä";
    }

    @Override
    public Parent createGameTable() {
        FlowPane parent = new FlowPane();
        parent.setOrientation(Orientation.VERTICAL);
        parent.setAlignment(Pos.BASELINE_CENTER);
        parent.setVgap(20);
        parent.setPadding(new Insets(50));
        parent.getChildren().addAll(getTitleLabel(), getGamesFlow(solitaires), getGamesFlow(otherGames));
        return parent;
    }

    private Node getTitleLabel() {
        Text title = new Text(getTitle());
        title.setFont(Font.font("Arial Bold", FontWeight.BOLD, FontPosture.ITALIC, 40));
        HBox box = new HBox(title);
        box.setPrefWidth(getWidth());
        box.setAlignment(Pos.BASELINE_CENTER);
        return box;
    }

    private FlowPane getGamesFlow(List<Class<? extends FXGameApplication>> games) {
        FlowPane flow = new FlowPane();
        flow.setAlignment(Pos.BASELINE_CENTER);
        flow.setPrefWidth(getWidth());
        flow.setPadding(new Insets(20));
        flow.getChildren().addAll(games.stream().map(this::getButtonFor).collect(toList()));
        flow.setHgap(20);
        return flow;
    }

    private Button getButtonFor(Class<? extends FXGameApplication> gameClass) {
        Button button = new Button();
        try {
            Optional<FXGameApplication> application = Optional.ofNullable(gameClass.newInstance());
            application.ifPresent(game -> {
                button.setText(game.getTitle());
                button.setWrapText(true);
                button.setTextAlignment(TextAlignment.CENTER);
                button.setPrefWidth(100);
                button.setPrefHeight(100);
                button.setOnAction(event -> launchGame(game, getPrimaryStageFor(button)));
            });
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return button;
    }

    private Stage getPrimaryStageFor(Button button) {
        return (Stage) button.getScene().getWindow();
    }

    private void launchGame(FXGameApplication game, Stage primaryStage) {
        Stage gameStage = new Stage();
        gameStage.setOnCloseRequest(e -> primaryStage.show());
        try {
            game.start(gameStage);
            primaryStage.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
