package org.chainoptim.desktop.features.test.tudor;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductTestController implements Initializable {
    @FXML
    private Label productNameLabel;

    private final CurrentSelectionService currentSelectionService;

    @Inject
    public ProductTestController(CurrentSelectionService currentSelectionService) {
        this.currentSelectionService = currentSelectionService;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer productId = currentSelectionService.getSelectedProductId();
        if (productId != null) {
            System.out.println("Load successful: " + productId);
        }
    }


}
