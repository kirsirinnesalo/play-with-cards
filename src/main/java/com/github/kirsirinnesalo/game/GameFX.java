package com.github.kirsirinnesalo.game;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GameFX extends Application {

    static final double DEFAULT_WIDTH = 800;
    static final double DEFAULT_HEIGHT = 600;

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
