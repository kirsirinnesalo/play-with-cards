package com.github.kirsirinnesalo.game;

import com.github.kirsirinnesalo.game.blackjack.BlackjackGUI;
import com.github.kirsirinnesalo.game.solitaire.AcesUp;
import com.github.kirsirinnesalo.game.solitaire.Eternity;
import com.github.kirsirinnesalo.game.solitaire.Klondike;
import com.github.kirsirinnesalo.game.solitaire.NapoleonsTomb;
import com.github.kirsirinnesalo.game.wof.WheelOfFortune;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class MyGames extends FXGameApplication {
    private static Map<Class<? extends FXGameApplication>, String> solitaires = new LinkedHashMap<>();
    private static Map<Class<? extends FXGameApplication>, String> otherGames = new LinkedHashMap<>();

    static {
        solitaires.put(AcesUp.class, "acesup");
        solitaires.put(Eternity.class, "ikuisuus");
        solitaires.put(NapoleonsTomb.class, "napoleon");
        solitaires.put(Klondike.class, "klondike");

        otherGames.put(BlackjackGUI.class, "blackjack");
        otherGames.put(WheelOfFortune.class, "onnenpyora");
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
        parent.getChildren().addAll(getTitleLabel(), getGamesFlow(solitaires.keySet()), getGamesFlow(otherGames.keySet()));
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

    private FlowPane getGamesFlow(Set<Class<? extends FXGameApplication>> games) {
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
            Map<Class<? extends FXGameApplication>, String> gameIconMap = new HashMap<>();
            gameIconMap.putAll(solitaires);
            gameIconMap.putAll(otherGames);
            Optional<FXGameApplication> application = Optional.ofNullable(gameClass.newInstance());
            application.ifPresent(game -> {
                button.setText(game.getTitle());
                button.setWrapText(true);
                button.setTextAlignment(TextAlignment.CENTER);
                button.setStyle("-fx-font-weight: bold;");
                button.setPrefWidth(120);
                button.setPrefHeight(100);
                setButtonIconForGame(button, gameIconMap, game);
                button.setOnAction(event -> launchGame(game, getPrimaryStageFor(button)));
            });
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return button;
    }

    private void setButtonIconForGame(Button button, Map<Class<? extends FXGameApplication>, String> gameIconMap, FXGameApplication game) {
        String imagePath = "icons/"+gameIconMap.get(game.getClass())+".png";
        ImageView icon = new ImageView(Utils.getImage(imagePath));
        icon.setPreserveRatio(true);
        icon.setFitHeight(80);
        button.setGraphic(icon);
        button.setGraphicTextGap(10);
        button.setContentDisplay(ContentDisplay.TOP);
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
