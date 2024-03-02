package org.chainoptim.desktop.shared.fallback;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LoadingFallbackController {

    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    AnchorPane anchorPane;

    @FXML
    public void initialize() {
        AnchorPane.setTopAnchor(loadingSpinner, null);
        AnchorPane.setBottomAnchor(loadingSpinner, null);
        AnchorPane.setLeftAnchor(loadingSpinner, null);
        AnchorPane.setRightAnchor(loadingSpinner, null);

        loadingSpinner.translateXProperty().bind(anchorPane.widthProperty().subtract(loadingSpinner.widthProperty()).divide(2));
        loadingSpinner.translateYProperty().bind(anchorPane.heightProperty().subtract(loadingSpinner.heightProperty().add(loadingSpinner.progressProperty().multiply(loadingSpinner.heightProperty()))).divide(2));

        loadingSpinner.setVisible(true);
        System.out.println("Loading spinner initialized");
          }

}
