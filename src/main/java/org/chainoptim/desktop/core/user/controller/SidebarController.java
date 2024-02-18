package org.chainoptim.desktop.core.user.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

public class SidebarController {

    @FXML
    public Button productsButton;
    @FXML
    public Button ordersButton;
    @FXML
    public Button factoriesButton;
    @FXML
    public Button warehousesButton;
    @FXML
    public Button supplierButton;
    @FXML
    private Button overviewButton;
    @FXML
    private Button organizationButton;
    public void initialize(){
        setButtonGraphic(overviewButton, "/img/globe-solid.png");
        setButtonGraphic(organizationButton, "/img/building-regular.png");
        setButtonGraphic(productsButton, "/img/box-solid.png");
        setButtonGraphic(ordersButton, "/img/cart-shopping-solid.png");
        setButtonGraphic(factoriesButton, "/img/industry-solid.png");
        setButtonGraphic(warehousesButton, "/img/warehouse-solid.png");
        setButtonGraphic(supplierButton, "/img/truck-arrow-right-solid.png");
    }

    public void setButtonGraphic(Button button, String imagePath){
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        button.setGraphic(imageView);
        button.setGraphicTextGap(10);
    }

    @FXML
    private void handleLogout() {
        AuthenticationService.logout(); // Clear JWT token from storage

        // Switch back to login scene
        try {
            SceneManager.loadLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
