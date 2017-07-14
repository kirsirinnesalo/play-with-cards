package com.github.kirsirinnesalo.game.wof;

import com.github.kirsirinnesalo.game.FXGameApplication;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class WheelOfFortune extends FXGameApplication {

    private static final List<String> sliceLabelTexts = new ArrayList<>();
    private static final AtomicInteger sliceLabelIndex = new AtomicInteger(0);
    private static final List<String> phrases = new ArrayList<>();
    private static final List<Player> players = new ArrayList<>();
    private static final int VOWEL_PRICE = 500;
    private static final String BORDER_STYLE = "-fx-border-color: dimgray; -fx-border-radius: 5;";
    private static final String[] sliceColors = {
            "#f9d900", //yellow
            "#a9e200", //green
            "#22bad9", //light blue
            "#0181e2", //blue
            "#860061", //purple
            "#ff5700"  //orange
    }; //x4

    static {
        sliceLabelTexts.addAll(Stream.of(
                "ROSVO", "ROSVO",
                "OHI", "OHI",
                "50", "100", "150", "200", "250", "300", "350", "400", "450", "500",
                "550", "600", "650", "700", "750", "800", "850", "900", "950", "1000"
        ).collect(toList()));
        Collections.shuffle(sliceLabelTexts);

        phrases.addAll(Stream.of(
                "Parempi ystävä pöydässä kuin 10 Facebookissa.",
                "Joukossa luovuus tiivistyy.",
                "Niin netti vastaa kuin sinne huudetaan.",
                "Hullu paljon työtä tekee, viisas järjestää niitä toisillekin.",
                "Joka kuuseen kurkottaa, saattaa latvaan asti ulottaa.",
                "Jos ei heilaa helluntaina, niin sitten joskus toiste.",
                "Kellä onni on, se onnen kylväköön.",
                "Innostunut töitään luettelee.",
                "Työ tekijäänsä kiittää, mutta palkka motivoi.",
                "Nopeat elävät, hitaat nauttivat.",
                "Some on hyvä renki, mutta huono isäntä."
        ).collect(toList()));
        Collections.shuffle(phrases);

        players.addAll(Stream.of(
                new Player("Pelaaja 1"),
                new Player("Pelaaja 2"),
                new Player("Pelaaja 3")
        ).collect(toList()));
    }

    private String currentPhrase;
    private StringProperty hiddenPhrase = new SimpleStringProperty();
    private IntegerProperty currentPlayerIndex = new SimpleIntegerProperty(0);

    private SpinningWheel wheel;
    private Button buyVowelButton;
    private Button resolveButton;
    private Pane consonantsLeft;
    private Pane vowelsLeft;
    private Node spinButton;
    private Node resolvePhraseBox;
    private TextField resolvePhraseField;
    private Text winnerText;

    private String usedLetters = "";

    @Override
    public String getTitle() {
        return "Onnenpyörä";
    }

    @Override
    public double getWidth() {
        return 800;
    }

    @Override
    public double getHeight() {
        return 600;
    }

    @Override
    public Parent createGameTable() {
        AnchorPane table = new AnchorPane();
        table.getChildren().addAll(createTitle(), createPlayers(), createPhrase(), createWheel(),
                createConsonantsBox(), createVowelBox(), createResolvePhraseField(), createButtons(),
                createWinnerText());
        newGame();
        return table;
    }

    private void newGame() {
        sliceLabelIndex.set(0);

        players.forEach(Player::resetPoints);
        currentPlayerIndex.set(0);
        usedLetters = "";
        currentPhrase = getRandomPhrase();
        resolvePhraseField.setText(null);
        setHiddenPhrase();

        hide(winnerText);
        enable(wheel, spinButton);
        enableLetters();

        Parent parent = wheel.getParent();
        parent.setOnMouseClicked(null);
        parent.setOnKeyPressed(null);
    }

    private void enableLetters() {
        consonantsLeft.getChildren().forEach(this::enable);
        vowelsLeft.getChildren().forEach(this::enable);
    }

    private Node createWinnerText() {
        winnerText = new Text();
        winnerText.setX(100);
        winnerText.setY(250);
        winnerText.setWrappingWidth(500);
        winnerText.setTextAlignment(TextAlignment.CENTER);
        winnerText.setFont(Font.font("Arial Bold", 100));
        winnerText.setStyle(
                " -fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);\n" +
                        " -fx-stroke: black;\n" +
                        " -fx-stroke-width: 1;");
        hide(winnerText);
        return winnerText;
    }

    private HBox createResolvePhraseField() {
        resolvePhraseField = new TextField();
        resolvePhraseField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                checkPhrase(resolvePhraseField.getText());
            }
        });
        HBox box = new HBox();
        box.setLayoutX(580);
        box.setLayoutY(280);
        box.setSpacing(10);
        Button resolvePhraseButton = new Button("Ratkaise");
        resolvePhraseButton.setOnAction(event -> checkPhrase(resolvePhraseField.getText()));
        box.getChildren().addAll(resolvePhraseField, resolvePhraseButton);
        hide(box);
        resolvePhraseBox = box;
        return box;
    }

    private void checkPhrase(String text) {
        if (lowerCaseAndRemoveSpecialCharsFrom(currentPhrase).equals(lowerCaseAndRemoveSpecialCharsFrom(text))) {
            disable(wheel, spinButton, resolveButton, buyVowelButton);
            hide(resolvePhraseBox);
            hiddenPhrase.set(currentPhrase);
            Player winner = players.get(currentPlayerIndex.get());
            winnerText.setText(winner.name + " on voittaja!");
            show(winnerText);
            Parent parent = wheel.getParent();
            parent.setOnMouseClicked(event -> newGame());
        } else {
            resolvePhraseField.setText("");
            hide(resolvePhraseBox);
            nextPlayer();
        }
    }

    private String lowerCaseAndRemoveSpecialCharsFrom(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zåäö 0-9]", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private Pane createConsonantsBox() {
        consonantsLeft = createLetterBox();
        consonantsLeft.getChildren().addAll(
                getConsonant("B"), getConsonant("C"),
                getConsonant("D"), getConsonant("F"),
                getConsonant("G"), getConsonant("H"),
                getConsonant("J"), getConsonant("K"),
                getConsonant("L"), getConsonant("M"),
                getConsonant("N"), getConsonant("P"),
                getConsonant("Q"), getConsonant("R"),
                getConsonant("S"), getConsonant("T"),
                getConsonant("V"), getConsonant("W"),
                getConsonant("X"), getConsonant("Z")
        );
        return consonantsLeft;
    }

    private Pane createVowelBox() {
        vowelsLeft = createLetterBox();
        vowelsLeft.getChildren().addAll(
                getVowel("A"), getVowel("E"),
                getVowel("I"), getVowel("O"),
                getVowel("U"), getVowel("Y"),
                getVowel("Å"), getVowel("Ä"),
                getVowel("Ö")
        );
        return vowelsLeft;
    }

    private Button getConsonant(String letter) {
        EventHandler<ActionEvent> actionEventEventHandler = event -> selectConsonant(letter);
        return getLetter(letter, actionEventEventHandler);
    }

    private Button getVowel(String letter) {
        EventHandler<ActionEvent> actionEventEventHandler = event -> selectVowel(letter);
        return getLetter(letter, actionEventEventHandler);
    }

    private FlowPane createLetterBox() {
        FlowPane box = new FlowPane();
        box.setOrientation(Orientation.HORIZONTAL);
        box.setVgap(10);
        box.setHgap(10);
        box.setPrefWidth(220);
        box.setPrefHeight(230);
        box.setLayoutX(570);
        box.setLayoutY(280);
        box.setPadding(new Insets(10));
        box.setStyle(BORDER_STYLE);
        box.managedProperty().bind(box.visibleProperty());
        hide(box);
        return box;
    }

    private Button getLetter(String letter, EventHandler<ActionEvent> actionEventEventHandler) {
        Button label = new Button(letter);
        label.setFont(Font.font("Courier New", 20));
        label.setCursor(Cursor.HAND);
        label.setOnAction(actionEventEventHandler);
        return label;
    }

    private void selectConsonant(String letter) {
        hide(consonantsLeft);
        Optional<Node> consonant = consonantsLeft.getChildren().stream()
                .filter(node -> ((Button) node).getText().equals(letter))
                .findFirst();
        consonant.ifPresent(this::disable);
        if (currentPhrase.toUpperCase().contains(letter)) {
            revealLetters(letter);
            enable(spinButton, resolveButton);
            if (players.get(currentPlayerIndex.get()).getPoints() >= VOWEL_PRICE) {
                enable(buyVowelButton);
            }
        } else {
            nextPlayer();
        }
    }

    private void selectVowel(String letter) {
        hide(vowelsLeft);
        Optional<Node> vowel = vowelsLeft.getChildren().stream()
                .filter(node -> ((Button) node).getText().equals(letter))
                .findFirst();
        vowel.ifPresent(this::disable);
        if (currentPhrase.toUpperCase().contains(letter)) {
            revealLetters(letter);
            enable(spinButton, resolveButton);
        } else {
            nextPlayer();
        }
    }

    private void revealLetters(String letter) {
        usedLetters += letter.toLowerCase() + letter.toUpperCase();
        setHiddenPhrase();
    }

    private VBox createButtons() {
        resolveButton = createButton("Ratkaise");
        resolveButton.setOnAction(event -> showResolvePhraseField());
        show(resolveButton);

        buyVowelButton = createButton("Osta vokaali");
        buyVowelButton.setOnAction(event -> buyVowel());
        disable(buyVowelButton);

        VBox box = new VBox();
        box.getChildren().addAll(resolveButton, buyVowelButton);
        box.setLayoutX(600);
        box.setLayoutY(175);
        box.setSpacing(20);
        box.setPadding(new Insets(10));
        box.setStyle(BORDER_STYLE);
        return box;
    }

    private void showResolvePhraseField() {
        disable(resolveButton, buyVowelButton);
        show(resolvePhraseBox);
        resolvePhraseField.requestFocus();
    }

    private void buyVowel() {
        disable(resolveButton);
        show(vowelsLeft);
        Player player = players.get(currentPlayerIndex.get());
        player.add(-VOWEL_PRICE);
        disable(buyVowelButton);
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.managedProperty().bind(button.visibleProperty());
        return button;
    }

    private VBox createPlayers() {
        VBox box = new VBox();
        players.forEach(player -> box.getChildren().add(createPlayerBox(player)));
        players.get(currentPlayerIndex.get()).setActive(true);
        box.setLayoutX(10);
        box.setLayoutY(120);
        box.setSpacing(5);
        return box;
    }

    private HBox createPlayerBox(Player player) {
        Text nameField = new Text(player.name + ":");
        nameField.setText(player.name + ":");
        nameField.setFont(Font.font("Arial", 14));
        nameField.setTextOrigin(VPos.BOTTOM);
        nameField.opacityProperty().bind(
                Bindings.when(player.active)
                        .then(1)
                        .otherwise(0.4)
        );
        nameField.fontProperty().bind(
                Bindings.when(player.active)
                        .then(Font.font("Arial", FontWeight.BOLD, 14))
                        .otherwise(Font.font("Arial", FontWeight.NORMAL, 14))
        );

        Label points = new Label();
        points.setFont(Font.font("Courier New", 14));
        points.setStyle("-fx-font-weight: bold; -fx-border-style: dashed; " + BORDER_STYLE);
        points.setAlignment(Pos.BOTTOM_RIGHT);
        points.setMinWidth(40);
        points.setPadding(new Insets(3));
        points.textProperty().bind(player.points.asString());

        HBox box = new HBox(nameField, points);
        box.setAlignment(Pos.BASELINE_CENTER);
        box.setSpacing(5);
        box.setPadding(new Insets(3, 20, 3, 20));
        String activeStyle = "-fx-background-radius: 5; -fx-border-radius: 5;" +
                " -fx-border-style: solid; -fx-border-color: lightblue;" +
                " -fx-background-color: lightblue;";
        box.styleProperty().bind(
                Bindings.when(player.active)
                        .then(activeStyle)
                        .otherwise("")
        );
        return box;
    }

    private HBox createPhrase() {
        currentPhrase = getRandomPhrase();
        setHiddenPhrase();
        Label phrase = new Label();
        phrase.textProperty().bind(hiddenPhrase);
        phrase.setFont(Font.font("Courier New", 20));
        phrase.setPrefWidth(350);
        phrase.setWrapText(true);
        phrase.setAlignment(Pos.BASELINE_CENTER);
        phrase.setTextAlignment(TextAlignment.CENTER);
        HBox phraseBox = new HBox();
        phraseBox.setPadding(new Insets(10));
        phraseBox.setStyle(BORDER_STYLE);
        phraseBox.setLayoutX(400);
        phraseBox.setLayoutY(50);
        phraseBox.getChildren().add(phrase);
        return phraseBox;
    }

    private void setHiddenPhrase() {
        String unusedLetters = "ABCDEFGHIJKLLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö";
        if (usedLetters.length() > 0) {
            unusedLetters = unusedLetters.replaceAll("[" + usedLetters + "]", "");
        }
        String hidden = currentPhrase.replaceAll("[" + unusedLetters + "]", "*");
        hiddenPhrase.set(hidden);
    }

    private Node createTitle() {
        Text title = new Text("Onnenpyörä");
        title.setEffect(new Reflection());
        title.setFont(Font.font("Arial Black", 30));
        title.setTextOrigin(VPos.TOP);
        title.setX(30);
        title.setY(10);
        return title;
    }

    private SpinningWheel createWheel() {
        wheel = new SpinningWheel(createSlices());
        changeSliceColors(wheel.getSlices());
        wheel.setLayoutX(100);
        wheel.setLayoutY(175);
        spinButton = wheel.getSpinButton();
        spinButton.setOnMouseEntered(mouseEvent -> spinButton.setCursor(Cursor.HAND));
        spinButton.requestFocus();
        spinButton.setOnMouseClicked(mouseEvent -> {
            Player player = players.get(currentPlayerIndex.get());
            wheel.spin(() -> handleSpinFor(player));
            disable(resolveButton, spinButton, buyVowelButton);

        });
        return wheel;
    }

    private void handleSpinFor(Player player) {
        PieChart.Data selectedData = wheel.getSelectedData();
        int value = getSliceValue(selectedData);
        enable(resolveButton);
        if (value > 0) {
            player.add(value);
            show(consonantsLeft);
            disable(resolveButton);
        } else {
            if ("ROSVO".equals(selectedData.getName())) {
                player.resetPoints();
            }
            nextPlayer();
        }
    }

    private void nextPlayer() {
        int nextPlayerIndex = IntStream.range(0, players.size())
                .filter(i -> players.get(i).isActive())
                .findFirst().orElse(-1) + 1;
        if (nextPlayerIndex >= players.size()) {
            nextPlayerIndex = 0;
        }
        players.get(currentPlayerIndex.get()).setActive(false);
        Player player = players.get(nextPlayerIndex);
        player.setActive(true);
        currentPlayerIndex.set(nextPlayerIndex);
        hide(consonantsLeft);
        if (player.getPoints() < VOWEL_PRICE) {
            disable(buyVowelButton);
        } else {
            enable(buyVowelButton);
        }
        enable(resolveButton, spinButton);
    }

    private int getSliceValue(PieChart.Data data) {
        String name = data.getName();
        int value;
        try {
            value = Integer.parseInt(name);
        } catch (NumberFormatException e) {
            switch (name) {
                case "ROSVO":
                    value = -1;
                    break;
                case "OHI":
                    value = 0;
                    break;
                default:
                    value = 0;
            }
        }
        return value;
    }

    private ObservableList<PieChart.Data> createSlices() {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
        IntStream.rangeClosed(1, 4).forEach(i ->
                stream(sliceColors).forEach(color -> {
                    String name = sliceLabelTexts.get(sliceLabelIndex.getAndAdd(1));
                    slices.add(new PieChart.Data(name, 1));
                })
        );
        return slices;
    }

    private void changeSliceColors(ObservableList<PieChart.Data> slices) {
        int i = 0;
        for (PieChart.Data data : slices) {
            String color = sliceColors[i % sliceColors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    private String getRandomPhrase() {
        List<String> tempPhrases = new ArrayList<>();
        tempPhrases.addAll(phrases);
        tempPhrases.remove(currentPhrase);
        Collections.shuffle(tempPhrases);
        return tempPhrases.get(0);
    }

    @Override
    protected void disable(Node... nodes) {
        super.disable(nodes);
        stream(nodes).filter(SpinButton.class::isInstance)
                .findFirst().ifPresent(spinBtn -> spinBtn.setOpacity(1));
    }
}

class Player {
    final String name;
    final IntegerProperty points = new SimpleIntegerProperty(0);
    final BooleanProperty active = new SimpleBooleanProperty(false);

    Player(String name) {
        this.name = name;
    }

    int getPoints() {
        return points.get();
    }

    void resetPoints() {
        points.set(0);
    }

    void add(int points) {
        this.points.set(this.points.get() + points);
    }

    boolean isActive() {
        return active.get();
    }

    void setActive(boolean value) {
        active.set(value);
    }
}
