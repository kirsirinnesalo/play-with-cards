package com.github.kirsirinnesalo.game;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class FXGameApplication extends Application {

    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getTitle());
        primaryStage.setScene(createScene());
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public abstract String getTitle();

    public abstract Parent createGameTable();

    public double getWidth() {
        return DEFAULT_WIDTH;
    }

    public double getHeight() {
        return DEFAULT_HEIGHT;
    }

    private Scene createScene() {
        return new Scene(createGameTable(), getWidth(), getHeight());
    }

}
