package org.chainoptim.desktop.core.user.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationService;

public class SidebarController {

    private boolean isSidebarMinimized = false;

    @FXML
    public AnchorPane sidebar;
    @FXML
    public Button accountButton;
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
    public Button dashboardButton;
    @FXML
    private Button overviewButton;
    @FXML
    private Button organizationButton;
    @FXML
    public Button logoutButton;

    // Initialize the sidebar
    public void initialize(){
        setButtonGraphic(dashboardButton, "/img/bars-solid.png");
        setButtonGraphic(overviewButton, "/img/globe-solid.png");
        setButtonGraphic(organizationButton, "/img/building-regular.png");
        setButtonGraphic(productsButton, "/img/box-solid.png");
        setButtonGraphic(ordersButton, "/img/cart-shopping-solid.png");
        setButtonGraphic(factoriesButton, "/img/industry-solid.png");
        setButtonGraphic(warehousesButton, "/img/warehouse-solid.png");
        setButtonGraphic(supplierButton, "/img/truck-arrow-right-solid.png");
        setButtonGraphic(logoutButton, "/img/right-from-bracket-solid.png");
        setButtonGraphic(accountButton, "/img/user-solid.png");
       
        // Set the max width of the buttons to Double.MAX_VALUE
        dashboardButton.setMaxWidth(Double.MAX_VALUE);
        overviewButton.setMaxWidth(Double.MAX_VALUE);
        organizationButton.setMaxWidth(Double.MAX_VALUE);
        productsButton.setMaxWidth(Double.MAX_VALUE);
        ordersButton.setMaxWidth(Double.MAX_VALUE);
        factoriesButton.setMaxWidth(Double.MAX_VALUE);
        warehousesButton.setMaxWidth(Double.MAX_VALUE);
        supplierButton.setMaxWidth(Double.MAX_VALUE);
        accountButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        logoutButton.setOnAction(event -> handleLogout());
        dashboardButton.setOnAction(event -> toggleSidebar());
    }

    // Set the graphic of a button
    public void setButtonGraphic(Button button, String imagePath){
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        button.setGraphic(imageView);
        button.setGraphicTextGap(10);
    }

    // Toggle the sidebar
    public void toggleSidebar(){
        if(isSidebarMinimized){
            dashboardButton.setText("Dashboard");
            overviewButton.setText("Overview");
            organizationButton.setText("Organization");
            productsButton.setText("Products");
            ordersButton.setText("Orders");
            factoriesButton.setText("Factories");
            warehousesButton.setText("Warehouses");
            supplierButton.setText("Supplier");
            accountButton.setText("Account");
            logoutButton.setText("Logout");

            sidebar.setPrefWidth(150);
        } else {
            dashboardButton.setText("");
            overviewButton.setText("");
            organizationButton.setText("");
            productsButton.setText("");
            ordersButton.setText("");
            factoriesButton.setText("");
            warehousesButton.setText("");
            supplierButton.setText("");
            accountButton.setText("");
            logoutButton.setText("");

            sidebar.setPrefWidth(40);
        }
        isSidebarMinimized = !isSidebarMinimized;
    }

    // Handle logout
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
