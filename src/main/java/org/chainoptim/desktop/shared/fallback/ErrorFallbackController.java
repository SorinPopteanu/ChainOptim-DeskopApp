package org.chainoptim.desktop.shared.fallback;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class ErrorFallbackController {
    @FXML
    private ImageView errorImage;

    @FXML
    private Label errorMessage;

    public void initialize(String errorMessage) {
        errorImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/error.png"))));
        errorImage.setOpacity(0.5);
        errorImage.setFitHeight(160);
        errorImage.setFitWidth(160);
        this.errorMessage.setText(errorMessage);
    }
}
