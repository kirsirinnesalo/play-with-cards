package com.github.kirsirinnesalo.scene.control;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;

import java.util.Optional;
import java.util.OptionalDouble;

public class FontSelector extends ListView<String> {

    public FontSelector(final Label target) {
        this(target, OptionalDouble.empty(), Optional.empty());
    }

    public FontSelector(final Label target, final OptionalDouble fontSize) {
        this(target, fontSize, Optional.empty());
    }

    public FontSelector(final Label target, final OptionalDouble fontSize, final Optional<String> defaultFontName) {
        super(FXCollections.observableArrayList(Font.getFontNames()));
        getSelectionModel().select(defaultFontName.orElse(getItems().get(0)));
        getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    Font newFont = new Font(getSelectionModel().getSelectedItem(), fontSize.orElse(25));
                    target.setFont(newFont);
                }

        );
        setMinHeight(30);
        setPrefWidth(200);
    }

}
