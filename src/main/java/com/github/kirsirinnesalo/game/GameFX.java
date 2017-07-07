package com.github.kirsirinnesalo.game;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public abstract class GameFX extends Application {

    public static final double DEFAULT_WIDTH = 800;
    public static final double DEFAULT_HEIGHT = 600;

    public static final CornerRadii STACK_CORNER_RADII = new CornerRadii(3);
    public static final Border EMPTY_STACK_BORDER = new Border(
            new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, STACK_CORNER_RADII, new BorderWidths(1))
    );
    public static final Background EMPTY_STACK_BACKGROUND = new Background(
            new BackgroundFill(Color.LIGHTGRAY, STACK_CORNER_RADII, Insets.EMPTY));

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getTitle());
        primaryStage.setScene(createScene());
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    protected abstract String getTitle();

    protected double getWidth() {
        return DEFAULT_WIDTH;
    }

    protected double getHeight() {
        return DEFAULT_HEIGHT;
    }

    protected abstract Parent createGameTable();

    private Scene createScene(){
        return new Scene(createGameTable(), getWidth(), getHeight());
    }

}
