package org.chainoptim.desktop.core.main.controller;

import org.chainoptim.desktop.core.main.model.SidebarNavigationConfiguration;
import org.chainoptim.desktop.core.main.model.SidebarSection;
import org.chainoptim.desktop.core.main.model.SidebarSubsection;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.main.service.SceneManager;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.*;

/**
 * Controller for Sidebar
 * Responsible for creating its buttons with NavigationService, img/ icons and sidebar.css
 * and toggling its expanded/collapsed view
 */
public class SidebarController {

    // Services
    @Setter
    private NavigationService navigationService;
    private final AuthenticationService authenticationService;

    // State
    private final List<Button> navigationButtons = new ArrayList<>();
    private final Map<String, Button> toggleButtons = new HashMap<>(); // Key: section name

    // Constants
    private final List<String> orderedKeys = List.of("Overview", "Organization", "Products", "Factories", "Warehouses", "Suppliers", "Clients", "Settings");

    private static final double COLLAPSED_WIDTH = 64;
    private static final double EXPANDED_WIDTH = 256;
    private boolean isSidebarMinimized = false;

    // FXML
    @FXML
    private VBox sidebar;
    @FXML
    private HBox dashboardHBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox buttonContainer;
    @FXML
    private Button toggleButton;
    @FXML
    private Button backButton;
    @FXML
    private VBox bottomContainer;
    @FXML
    private Button logoutButton;

    // Icons
    private Image caretUpIcon;
    private Image caretDownIcon;

    @Inject
    public SidebarController(NavigationService navigationService, AuthenticationService authenticationService) {
        this.navigationService = navigationService;
        this.authenticationService = authenticationService;
    }

    // Initialization
    @FXML
    public void initialize() {
        sidebar.setMinWidth(EXPANDED_WIDTH);
        sidebar.setMaxWidth(EXPANDED_WIDTH);

        scrollPane.getStyleClass().add("edge-to-edge");

        initializeIcons();
        createSidebarButtons();

        // Navigate to Overview
        navigationService.switchView("Overview", true);

        // Back, Toggle and Logout buttons
        setButtonGraphic(backButton, SidebarNavigationConfiguration.getButtonIconPath("Back"));
        backButton.setOnAction(e -> navigationService.goBack());
        setButtonGraphic(toggleButton, SidebarNavigationConfiguration.getButtonIconPath("Toggle"));
        toggleButton.setOnAction(e -> toggleSidebar());
        setButtonGraphic(logoutButton, SidebarNavigationConfiguration.getButtonIconPath("Logout"));
    }

    private void initializeIcons() {
        caretUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/caret-up-solid.png")));
        caretDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/caret-down-solid.png")));
    }

    private void createSidebarButtons() {
        SidebarSection[] sections = SidebarNavigationConfiguration.getSidebarSections(navigationService);
        for (SidebarSection section : sections) {
            VBox sectionVBox = new VBox();
            sectionVBox.setStyle("-fx-padding: 8px 0px;");

            // Main section
            HBox mainHBox = new HBox();
            mainHBox.setAlignment(Pos.CENTER_LEFT);

            Button button = getSidebarButton(section);
            navigationButtons.add(button);
            mainHBox.getChildren().add(button);

            if (section.getSubsections().isEmpty()) {
                sectionVBox.getChildren().add(mainHBox);
                buttonContainer.getChildren().add(sectionVBox);
                continue;
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            mainHBox.getChildren().add(spacer);

            // Subsection toggling
            VBox subSectionVBox = new VBox();
            toggleNodeVisibility(subSectionVBox, section.isExpanded());
            subSectionVBox.getStyleClass().add("sidebar-subsection");

            Button toggleSectionButton = new Button();
            toggleSectionButton.setGraphic(createImageView(caretDownIcon));
            toggleSectionButton.getStyleClass().add("sidebar-toggle-button");
            toggleSectionButton.setOnAction(e -> toggleSection(subSectionVBox, section));
            toggleButtons.put(section.getName(), toggleSectionButton);
            mainHBox.getChildren().add(toggleSectionButton);

            // Subsections
            for (SidebarSubsection subsection : section.getSubsections()) {
                Button subsectionButton = getSidebarSubButton(subsection);
                navigationButtons.add(subsectionButton);
                subSectionVBox.getChildren().add(subsectionButton);
            }

            sectionVBox.getChildren().addAll(mainHBox, subSectionVBox);
            buttonContainer.getChildren().add(sectionVBox);
        }
    }

    private Button getSidebarButton(SidebarSection section) {
        Button button = new Button(section.getName());
        setButtonGraphic(button, section.getIconPath());
        button.getStyleClass().add("sidebar-button");
        button.setStyle("-fx-padding: 10px 18px;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> section.getAction().run());

        return button;
    }

    private Button getSidebarSubButton(SidebarSubsection subSection) {
        Button button = new Button(subSection.getName());
        setButtonGraphic(button, subSection.getIconPath());
        button.getStyleClass().add(subSection.isSelected() ? "sidebar-subbutton-selected" : "sidebar-subbutton");
        subSection.getIsSelectedProperty().addListener((observable, oldValue, newValue) -> {
            button.getStyleClass().setAll(newValue ? "sidebar-subbutton-selected" : "sidebar-subbutton");
        });
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> subSection.getAction().run());
        button.setUserData(subSection.getKey());

        return button;
    }

    private void toggleSection(VBox subSectionVBox, SidebarSection section) {
        section.setExpanded(!section.isExpanded());
        toggleNodeVisibility(subSectionVBox, section.isExpanded());
        Button correspondingToggleButton = toggleButtons.get(section.getName());
        correspondingToggleButton.setGraphic(createImageView(section.isExpanded() ? caretUpIcon : caretDownIcon));
    }

    public void setButtonGraphic(Button button, String imagePath){
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(16);
            imageView.setFitWidth(16);
            button.setGraphic(imageView);
            button.setGraphicTextGap(10);
        } catch (Exception e) {
            System.out.println("Icon not added yet: " + imagePath);
        }
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
        sidebar.setMinWidth(COLLAPSED_WIDTH);
        sidebar.setMaxWidth(COLLAPSED_WIDTH);

        dashboardHBox.getStyleClass().setAll("sidebar-title-container-collapsed");
        dashboardHBox.getChildren().forEach(child -> child.setVisible(false));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(false));
        toggleButton.setVisible(true);
        toggleButton.setManaged(true);

        buttonContainer.getStyleClass().setAll("sidebar-inner-container-collapsed");
        buttonContainer.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                Tooltip tooltip = new Tooltip(button.getText());
                button.setTooltip(tooltip);
                button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                button.setStyle("-fx-padding: 10px;");
            }
        });
        bottomContainer.getStyleClass().setAll("sidebar-inner-container-collapsed");
        logoutButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    private void expandSidebar() {
        sidebar.setMinWidth(EXPANDED_WIDTH);
        sidebar.setMaxWidth(EXPANDED_WIDTH);

        dashboardHBox.getStyleClass().setAll("sidebar-title-container");
        dashboardHBox.getChildren().forEach(child -> child.setVisible(true));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(true));

        buttonContainer.getStyleClass().setAll("sidebar-inner-container");
        buttonContainer.getChildren().forEach(node -> {
            if (node instanceof Button button) {
                button.setContentDisplay(ContentDisplay.LEFT);
                button.setTooltip(null);
                button.setStyle("-fx-padding: 10px 18px;");
            }
        });
        bottomContainer.getStyleClass().setAll("sidebar-inner-container");
        logoutButton.setContentDisplay(ContentDisplay.LEFT);
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

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void toggleNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
