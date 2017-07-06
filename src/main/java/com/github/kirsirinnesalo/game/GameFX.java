package com.github.kirsirinnesalo.game;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GameFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getTitle());
        primaryStage.setScene(createScene());
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    protected abstract String getTitle();

    protected abstract double getWidth();

    protected abstract double getHeight();

    protected abstract Parent createGameTable();

    private Scene createScene(){
        return new Scene(createGameTable(), getWidth(), getHeight());
    }

}
