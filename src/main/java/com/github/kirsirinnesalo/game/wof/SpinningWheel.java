package com.github.kirsirinnesalo.game.wof;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.effect.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

class SpinningWheel extends Group {

    private final Wheel theWheel;

    SpinningWheel(ObservableList<Data> data) {
        theWheel = new Wheel(data);
        getChildren().addAll(theWheel, theWheel.spinButton, theWheel.spinLabel, theWheel.selector);
    }

    Wheel getWheel() {
        return theWheel;
    }

    Circle getSpinButton() {
        return theWheel.spinButton;
    }

    ObservableList<Data> getData() {
        return theWheel.getData();
    }

    Data getSelectedData() {
        return theWheel.getSelectedData();
    }

}

/* https://gist.github.com/jewelsea/b218c810b9d1009138bd */
class Wheel extends PieChart {
    private static final int SPIN_BUTTON_RADIUS = 8;

    final Circle spinButton = new Circle();
    final Polygon selector = new Polygon();
    final Text spinLabel = new Text("Pyöritä");
    private final Circle wheelCenter = new Circle();
    private List<Text> sliceLabels = new ArrayList<>();
    private Point2D selectorPoint;

    Wheel(ObservableList<Data> data) {
        super(data);
        setLegendVisible(false);
        setLabelsVisible(false);

        createLabels();

        wheelCenter.setFill(Color.WHITESMOKE);

        spinButton.setFill(Color.CYAN);
        spinButton.disabledProperty().addListener((observable, wasDisabled, disable) -> {
            if (disable) {
                spinButton.setFill(Color.LIGHTSEAGREEN);
            } else {
                spinButton.setFill(Color.CYAN);
            }
        });
        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135.0f);
        Lighting lighting = new Lighting();
        lighting.setSurfaceScale(5.0f);
        lighting.setLight(light);
        DropShadow dropShadow = new DropShadow();
        dropShadow.setBlurType(BlurType.THREE_PASS_BOX);
        dropShadow.setRadius(10);
        dropShadow.setColor(Color.DIMGRAY);
        lighting.setContentInput(dropShadow);
        spinButton.setEffect(lighting);

        spinLabel.setMouseTransparent(true);
        spinLabel.setFont(Font.font("Arial Black", 14));
        spinLabel.setTextOrigin(VPos.BASELINE);

        selector.setFill(Color.DIMGRAY);
        selector.setEffect(new InnerShadow());
        Double[] selectorCorners = {
                0d, 8.5d,
                0d, 6.5d,
                35d, 0d,
                35d, 15d
        };
        selector.getPoints().addAll(selectorCorners);
        selectorPoint = selector.localToScene(0, 6.5);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);
        addWheelCenter();
        relocateContent();
    }

    private void createLabels() {
        ObservableList<Data> data = getData();
        int labelCount = data.size();
        for (int i = 1; i <= labelCount; i++) {
            double angle = i * 360.0 / labelCount;
            Text label = new Text(data.get(i - 1).getName());
            label.getTransforms().addAll(
                    Transform.rotate(angle - 6, 0, 0),
                    Translate.translate(120, 0)
            );
            sliceLabels.add(label);
        }
        getChildren().addAll(sliceLabels);
    }

    private void addWheelCenter() {
        if (getData().size() > 0) {
            Node slice = getData().get(0).getNode();
            if (slice.getParent() instanceof Pane) {
                Pane parent = (Pane) slice.getParent();

                if (!parent.getChildren().contains(wheelCenter)) {
                    parent.getChildren().addAll(wheelCenter);
                }
            }
        }
    }

    private void relocateContent() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Data data : getData()) {
            Bounds bounds = data.getNode().getBoundsInParent();
            minX = Math.min(minX, bounds.getMinX());
            minY = Math.min(minY, bounds.getMinY());
            maxX = Math.max(maxX, bounds.getMaxX());
            maxY = Math.max(maxY, bounds.getMaxY());
        }
        double centerX = getCenter(minX, maxX);
        double centerY = getCenter(minY, maxY);
        double radius = (maxX - minX) / SPIN_BUTTON_RADIUS;

        wheelCenter.setCenterX(centerX);
        wheelCenter.setCenterY(centerY);
        wheelCenter.setRadius(radius * 2);

        sliceLabels.forEach(label -> label.relocate(centerX + 7, centerY - 10));

        spinButton.setRadius(radius);
        spinButton.relocate(centerX - radius + radius / 8, centerY - radius + radius / 8);

        relocateSpinLabel();
        relocateSelector();
    }

    private void relocateSpinLabel() {
        Point2D centerPoint = getCenterPoint();
        Bounds bounds = spinLabel.getBoundsInLocal();
        spinLabel.relocate(centerPoint.getX() - bounds.getWidth() / 2,
                centerPoint.getY() - bounds.getHeight() / 2);
    }

    private void relocateSelector() {
        Point2D centerPoint = getCenterPoint();
        double x = centerPoint.getX() + 3 * getWidth() / 8 - selector.getLayoutBounds().getWidth() / 2;
        selector.relocate(x - 5, centerPoint.getY() - 2);
    }

    private Point2D getCenterPoint() {
        Bounds bounds = getBoundsInParent();
        double centerX = getCenter(bounds.getMinX(), bounds.getMaxX());
        double centerY = getCenter(bounds.getMinY(), bounds.getMaxY());
        return new Point2D(centerX, centerY);
    }

    private double getCenter(double min, double max) {
        return min + (max - min) / 2;
    }

    Data getSelectedData() {
        final Data selectedData = new Data("", 0);
        getData().forEach(data -> {
            Node node = data.getNode();
            Point2D localPoint = node.sceneToLocal(selector.localToScene(selectorPoint));
            if (node.contains(localPoint)) {
                selectedData.setName(data.getName());
                selectedData.setPieValue(data.getPieValue());
            }
        });
        return selectedData;
    }

}
