package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.chainoptim.desktop.core.main.model.SidebarButton;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.main.service.SceneManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
 * Controller for Sidebar
 * Responsible for creating its buttons with NavigationService, img/ icons and sidebar.css
 * and toggling its expanded/collapsed view
 */
public class SidebarController {

    @Setter
    private NavigationService navigationService;
    private final AuthenticationService authenticationService;

    @Inject
    public SidebarController(NavigationService navigationService, AuthenticationService authenticationService) {
        this.navigationService = navigationService;
        this.authenticationService = authenticationService;
    }

    // UI elements
    @FXML
    private AnchorPane sidebar;
    @FXML
    private HBox dashboardHBox;
    @FXML
    private VBox buttonContainer;

    private final List<SidebarButton> navigationButtons = new ArrayList<>();
    @FXML
    public Button toggleButton;
//    @FXML
//    public Button accountButton;
    @FXML
    public Button logoutButton;

    // Configuration
    private final List<String> orderedKeys = List.of("Overview", "Organization", "Products", "Factories", "Warehouses", "Suppliers", "Clients");
    private final Map<String, String> buttonIconMap = Map.of(
            "Overview", "globe-solid.png",
            "Organization", "building-regular.png",
            "Products", "box-solid.png",
            "Factories", "industry-solid.png",
            "Warehouses", "warehouse-solid.png",
            "Suppliers", "truck-arrow-right-solid.png",
            "Clients", "universal-access-solid.png",
            "Account", "user-solid.png",
            "Toggle", "bars-solid.png"
    );
    private static final double COLLAPSED_WIDTH = 52;
    private static final double EXPANDED_WIDTH = 256;
    private boolean isSidebarMinimized = false;

    // Initialization
    @FXML
    public void initialize() {
        initializeNavigationButtons();
        createSidebarButtons();
        setButtonGraphic(logoutButton, "/img/right-from-bracket-solid.png");

        // Navigate to Overview
        navigationService.switchView("Overview");

        // Toggle button
        setButtonGraphic(toggleButton, "/img/" + buttonIconMap.get("Toggle"));
        toggleButton.setOnAction(e -> toggleSidebar());
    }

    private void initializeNavigationButtons() {
        orderedKeys.forEach(key -> {
            String iconPath = "/img/" + buttonIconMap.get(key);
            Runnable action = () -> navigationService.switchView(key);
            navigationButtons.add(new SidebarButton(key, iconPath, action));
        });
    }

    private void createSidebarButtons() {
        navigationButtons.forEach(model -> {
            Button button = new Button(model.getName());
            button.setOnAction(e -> model.getAction().run());
            setButtonGraphic(button, model.getIconPath());
            button.getStyleClass().add("sidebar-button");
            button.setMaxWidth(Double.MAX_VALUE);
            buttonContainer.getChildren().add(button);
        });
    }

    public void setButtonGraphic(Button button, String imagePath){
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        button.setGraphic(imageView);
        button.setGraphicTextGap(10);
    }

    // Sidebar toggling
    private void toggleSidebar() {
        if (isSidebarMinimized) {
            expandSidebar();
        } else {
            collapseSidebar();
        }
        isSidebarMinimized = !isSidebarMinimized;
    }

    private void collapseSidebar() {
        // Reduce width and hide everything but the icons
        sidebar.setPrefWidth(COLLAPSED_WIDTH);
        dashboardHBox.getChildren().forEach(child -> child.setVisible(false));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(false));
        toggleButton.setVisible(true);
        toggleButton.setManaged(true);

        buttonContainer.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
    }

    private void expandSidebar() {
        sidebar.setPrefWidth(EXPANDED_WIDTH);
        dashboardHBox.getChildren().forEach(child -> child.setVisible(true));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(true));

        buttonContainer.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                button.setContentDisplay(ContentDisplay.LEFT);
            }
        });
    }

    // Handle logout
    @FXML
    private void handleLogout() {
        authenticationService.logout(); // Clear JWT token from storage

        // Switch back to login scene
        try {
            SceneManager.loadLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
