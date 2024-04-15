package org.chainoptim.desktop.shared.common.uielements.performance;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;

public class ScoreDisplay extends StackPane {

    private Arc arcDisplay;
    private Label scoreLabel;

    public ScoreDisplay() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        arcDisplay = new Arc();
        arcDisplay.setCenterX(16);
        arcDisplay.setCenterY(16);
        arcDisplay.setRadiusX(16);
        arcDisplay.setRadiusY(16);
        arcDisplay.setStartAngle(90);
        arcDisplay.setLength(360);
        arcDisplay.setStrokeWidth(5);
        arcDisplay.setStroke(Color.LIGHTGRAY);
        arcDisplay.setFill(null);

        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("general-label");

        this.getChildren().addAll(arcDisplay, scoreLabel);
    }

    public void setScore(int score) {
        scoreLabel.setText(String.valueOf(score));
        arcDisplay.getStyleClass().removeAll("good-arc", "below-average-arc", "above-average-arc", "bad-arc");
        if (score < 25) {
            arcDisplay.getStyleClass().add("bad-arc");
        } else if (score < 50) {
            arcDisplay.getStyleClass().add("below-average-arc");
        } else if (score < 75) {
            arcDisplay.getStyleClass().add("above-average-arc");
        } else {
            arcDisplay.getStyleClass().add("good-arc");
        }
    }
}
