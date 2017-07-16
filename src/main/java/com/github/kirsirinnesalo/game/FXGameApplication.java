package com.github.kirsirinnesalo.game;

import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Arrays;

public abstract class FXGameApplication extends Application {

    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getTitle());
        primaryStage.setScene(createScene());
        primaryStage.centerOnScreen();
        primaryStage.getIcons().add(getApplicationIcon());
        primaryStage.show();
    }

    protected Image getApplicationIcon() {
        return Utils.getImage("icons/cardicon.png");
    }

    public abstract String getTitle();

    public abstract Parent createGameTable();

    public double getWidth() {
        return DEFAULT_WIDTH;
    }

    public double getHeight() {
        return DEFAULT_HEIGHT;
    }

    protected void disable(Node... nodes) {
        Arrays.stream(nodes).forEach(node -> {
            node.setDisable(true);
            node.setOpacity(0.4);
        });
    }

    protected void enable(Node... nodes) {
        Arrays.stream(nodes).forEach(node -> {
            node.setDisable(false);
            node.setOpacity(1);
        });
    }

    protected void show(Node... nodes) {
        setVisible(true, nodes);
    }

    protected void hide(Node... nodes) {
        setVisible(false, nodes);
    }

    private void setVisible(boolean visible, Node... nodes) {
        Arrays.stream(nodes).forEach(node -> node.setVisible(visible));
    }

    private Scene createScene() {
        return new Scene(createGameTable(), getWidth(), getHeight());
    }

}
