package com.github.kirsirinnesalo.scene.control;

import com.github.kirsirinnesalo.scene.util.Utils;

import org.apache.commons.lang3.text.WordUtils;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toSet;

public class ColorSelector extends ListView<String> {
    public ColorSelector(Region target) {
        Map<String, Color> colorMap = Arrays.stream(Color.class.getDeclaredFields())
                .filter(field -> isPublic(field.getModifiers()) && isStatic(field.getModifiers()))
                .collect(Collectors.toMap(Field::getName, this::getColor));

        Set<String> colorNames = colorMap.keySet().stream()
                .map(WordUtils::capitalizeFully)
                .collect(toSet());
        setItems(FXCollections.observableArrayList(new TreeSet<>(colorNames)));

        getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldvalue, newvalue) -> {
                    String selectedColorName = getSelectionModel().getSelectedItem();
                    target.setBackground(Utils.getBackgroundWith(colorMap.get(selectedColorName.toUpperCase())));
                }
        );
    }

    private Color getColor(Field field) {
        try {
            return (Color) field.get(Color.class);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError();
        }
    }
}
