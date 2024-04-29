package org.chainoptim.desktop.core.main.controller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

public class StartUpController {

    @FXML
    private ImageView customSpinner;

    @FXML
    public void initialize() {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/spinner-solid.png")));
        customSpinner.setImage(image);
        customSpinner.setFitHeight(48);
        customSpinner.setFitWidth(48);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.9);
        customSpinner.setEffect(colorAdjust);
        applySpinAnimation(customSpinner);
    }

    public void applySpinAnimation(Node node) {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), node);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
    }
}
