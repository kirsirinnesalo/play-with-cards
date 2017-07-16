package com.github.kirsirinnesalo.scene.util;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class Utils {

    private Utils() {}

    public static final Background TRANSPARENT_BACKGROUND
            = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

    public static Background getBackgroundWith(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    public static Image getImage(String imagePath) {
        return new Image(getResource(imagePath));
    }

    public static String getResource(String filePathName) {
        ClassLoader classLoader = Utils.class.getClassLoader();
        return classLoader.getResource(filePathName).toExternalForm();
    }


}
