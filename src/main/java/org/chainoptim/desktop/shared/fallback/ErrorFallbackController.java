package org.chainoptim.desktop.shared.fallback;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ErrorFallbackController {
    @FXML
    private ImageView errorImage;

    public void initialize() {
        errorImage.setImage(new Image(getClass().getResourceAsStream("/img/error.png")));
        errorImage.setOpacity(0.5);
    }
}
