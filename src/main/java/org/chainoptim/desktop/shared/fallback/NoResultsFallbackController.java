package org.chainoptim.desktop.shared.fallback;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class NoResultsFallbackController {
    @FXML
    private ImageView noResultsImage;

    public void initialize() {
        noResultsImage.setImage(new Image(getClass().getResourceAsStream("/img/no-results.png")));
        noResultsImage.setOpacity(0.5);
    }

}
