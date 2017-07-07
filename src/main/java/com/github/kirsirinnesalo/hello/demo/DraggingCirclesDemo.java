package com.github.kirsirinnesalo.hello.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class DraggingCirclesDemo extends Application {
    private Pane leftPane;
    private Pane rightPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Dragging demo");
        primaryStage.setScene(createScene());
        primaryStage.show();
    }

    private Scene createScene() {
        return new Scene(createRoot(), 600, 400);
    }

    private Parent createRoot() {
        Circle red = createCircle(40, Paint.valueOf("red"), Color.DARKRED);
        Circle yellow = createCircle(50, Paint.valueOf("yellow"), Color.GOLD);
        Circle green = createCircle(60, Paint.valueOf("green"), Color.DARKGREEN);

        leftPane = createPane();
        rightPane = createPane();

        leftPane.getChildren().addAll(red, yellow, green);
        red.relocate(75-red.getRadius(), 20);
        yellow.relocate(75-yellow.getRadius(), 20+ 20+red.getRadius()*2);
        green.relocate(75-green.getRadius(), 20+20+red.getRadius()*2+20+yellow.getRadius()*2);

        Circle blue = createCircle(50, Paint.valueOf("blue"), Color.NAVY);
        makeDraggable(blue);

        Circle purple = createCircle(50, Color.MEDIUMPURPLE, Paint.valueOf("purple"));
        makeDraggable(purple);

        StackPane group = new StackPane();
        group.getChildren().addAll(blue, purple);
        blue.relocate(50, 50);
        purple.relocate(150, 150);

        BorderPane root = new BorderPane();
        root.setLeft(leftPane);
        root.setRight(rightPane);
        root.setCenter(group);
        BorderPane.setMargin(leftPane, new Insets(13));
        BorderPane.setMargin(rightPane, new Insets(13));
        return root;
    }

    private Circle createCircle(int radius, Paint fillColor, Paint borderColor) {
        Circle circle = new Circle(radius, fillColor);
        circle.setId(String.valueOf(circle.hashCode()));
        circle.setStrokeWidth(3);
        circle.setStroke(borderColor);
        circle.setOnMouseEntered(e -> circle.setCursor(Cursor.HAND));
        circle.setOnDragDetected(onDragDetected);
        circle.setOnDragDone(onDragDone);
        //makeDraggable(circle);
        return circle;
    }

    private Pane createPane() {
        Pane pane = new Pane();
        pane.setId(String.valueOf(pane.hashCode()));
        pane.setPrefWidth(150);
/*
        pane.setOrientation(Orientation.VERTICAL);
        pane.setVgap(20);
        pane.setColumnHalignment(HPos.CENTER);
*/
        pane.setPadding(new Insets(10));
        pane.setBorder(new Border(new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, new CornerRadii(10), BorderStroke.THIN)));
        pane.addEventHandler(DragEvent.DRAG_OVER, onDragOver);
        pane.addEventHandler(DragEvent.DRAG_DROPPED, onDragDropped);
        return pane;
    }

    private EventHandler<MouseEvent> onDragDetected = event -> {
        Circle circle = (Circle) event.getSource();
        Dragboard dragboard = circle.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(circle.getParent().getId());
        dragboard.setContent(content);
        event.consume();
    };

    private EventHandler<DragEvent> onDragOver = event -> {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    };

    private EventHandler<DragEvent> onDragDropped = event -> {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;
        if (dragboard.hasString()) {
            Circle circle = (Circle) event.getGestureSource();
            Pane targetPane = (Pane) event.getGestureTarget();
            if (!circle.getParent().equals(targetPane)) {
                Platform.runLater(() -> targetPane.getChildren().add(circle));
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    };

    private EventHandler<DragEvent> onDragDone = event -> {
        TransferMode transferMode = event.getTransferMode();
        if (TransferMode.MOVE == transferMode) {
            Circle circle = (Circle) event.getGestureSource();
            Parent parent = circle.getParent();
            if (parent.equals(leftPane)) {
                leftPane.getChildren().remove(circle);
            } else {
                rightPane.getChildren().remove(circle);
            }
        }
        event.consume();
    };

    private void makeDraggable(Node node) {
        node.setOnMousePressed(onMousePressed);
        node.setOnMouseDragged(onMouseDragged);
        node.setOnMouseReleased(onMouseReleased);
    }

    private Point2D lastXY;
    private EventHandler<MouseEvent> onMousePressed = event -> {
        lastXY = new Point2D(event.getSceneX(), event.getSceneY());
        ((Node) event.getSource()).toFront();
    };

    private EventHandler<MouseEvent> onMouseDragged = event -> {
        Node node = (Node) event.getSource();
        double dx = event.getSceneX() - lastXY.getX();
        double dy = event.getSceneY() - lastXY.getY();
        node.setTranslateX(node.getTranslateX() + dx);
        node.setTranslateY(node.getTranslateY() + dy);
        lastXY = new Point2D(event.getSceneX(), event.getSceneY());
        event.consume();
    };

    private EventHandler<MouseEvent> onMouseReleased = event -> lastXY = null;

}
