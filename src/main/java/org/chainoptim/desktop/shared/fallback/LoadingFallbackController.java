package org.chainoptim.desktop.shared.fallback;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

public class LoadingFallbackController {

    @FXML
    private ImageView customSpinner;

    @FXML
    public void initialize() {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/spinner-solid.png")));
        customSpinner.setImage(image);
        customSpinner.setFitHeight(52);
        customSpinner.setFitWidth(52);
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
