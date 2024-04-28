package org.chainoptim.desktop.core.main.controller;

import org.chainoptim.desktop.core.main.model.SidebarNavigationConfiguration;
import org.chainoptim.desktop.core.main.model.SidebarSection;
import org.chainoptim.desktop.core.main.model.SidebarSubsection;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.main.service.SceneManager;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
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
    private final List<Button> mainNavigationButtons = new ArrayList<>();
    private final List<Region> sectionRegions = new ArrayList<>();
    private final Map<String, Button> toggleExpandSectionButtons = new HashMap<>(); // Key: section name
    private final List<VBox> subsectionVBoxes = new ArrayList<>();
    private boolean isSidebarCollapsed = false;
    private SidebarSection[] sections;

    // Constants
    private static final double COLLAPSED_WIDTH = 64;
    private static final double EXPANDED_WIDTH = 256;

    // FXML
    @FXML
    private VBox sidebar;
    @FXML
    private HBox dashboardHBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox mainVBox;
    @FXML
    private VBox navigationButtonContainer;
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
        configureSidebar();
        initializeIcons();
        initializeOuterButtons();
        renderSidebarButtons();

        navigationService.switchView("Overview", true, null);
    }

    private void configureSidebar() {
        sidebar.setMinWidth(EXPANDED_WIDTH);
        sidebar.setMaxWidth(EXPANDED_WIDTH);

        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPane.getContent().boundsInLocalProperty().addListener((obs, oldBounds, newBounds) ->
                updateScrollBarVisibility(scrollPane, newBounds));
    }

    private void initializeIcons() {
        caretUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/caret-up-solid.png")));
        caretDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/caret-down-solid.png")));
    }

    private void initializeOuterButtons() {
        // Back, Toggle and Logout buttons
        setButtonGraphic(backButton, SidebarNavigationConfiguration.getButtonIconPath("Back"));
        backButton.setOnAction(e -> navigationService.goBack());
        setButtonGraphic(toggleButton, SidebarNavigationConfiguration.getButtonIconPath("Toggle"));
        toggleButton.setOnAction(e -> toggleSidebar());
        setButtonGraphic(logoutButton, SidebarNavigationConfiguration.getButtonIconPath("Logout"));
    }

    private void renderSidebarButtons() {
        sections = SidebarNavigationConfiguration.getSidebarSections(navigationService);
        for (SidebarSection section : sections) {
            VBox sectionVBox = new VBox();
            sectionVBox.setStyle("-fx-padding: 8px 0px;");

            // Main section
            HBox mainHBox = new HBox();
            VBox subSectionVBox = new VBox();
            renderMainHBox(mainHBox, section, sectionVBox, subSectionVBox);

            // Subsections
            if (section.getSubsections().isEmpty()) continue;
            for (SidebarSubsection subsection : section.getSubsections()) {
                Button subsectionButton = getSidebarSubButton(subsection);
                subSectionVBox.getChildren().add(subsectionButton);
            }
            sectionVBox.getChildren().addAll(mainHBox, subSectionVBox);
            subsectionVBoxes.add(subSectionVBox);

            navigationButtonContainer.getChildren().add(sectionVBox);
        }
    }

    private void renderMainHBox(HBox mainHBox, SidebarSection section, VBox sectionVBox, VBox subSectionVBox) {
        mainHBox.setAlignment(Pos.CENTER_LEFT);

        Button button = getSidebarButton(subSectionVBox, section);
        mainNavigationButtons.add(button);
        mainHBox.getChildren().add(button);

        if (section.getSubsections().isEmpty()) {
            sectionVBox.getChildren().add(mainHBox);
            navigationButtonContainer.getChildren().add(sectionVBox);
            return;
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        mainHBox.getChildren().add(spacer);
        sectionRegions.add(spacer);

        // Subsection toggling
        toggleNodeVisibility(subSectionVBox, section.isExpanded());
        subSectionVBox.getStyleClass().add("sidebar-subsection");

        Button toggleSectionButton = new Button();
        toggleSectionButton.setGraphic(createImageView(caretDownIcon));
        toggleSectionButton.getStyleClass().add("sidebar-toggle-button");
        toggleSectionButton.setOnAction(e -> toggleSection(subSectionVBox, section));
        toggleExpandSectionButtons.put(section.getName(), toggleSectionButton);
        mainHBox.getChildren().add(toggleSectionButton);
    }

    private Button getSidebarButton(VBox subSectionVBox, SidebarSection section) {
        Button button = new Button(section.getName());
        setButtonGraphic(button, section.getIconPath());
        button.getStyleClass().add("sidebar-button");
        button.setStyle("-fx-padding: 10px 18px;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            section.getAction().run();
            if (!section.isExpanded()) {
                toggleSection(subSectionVBox, section);
            }
        });

        return button;
    }

    private Button getSidebarSubButton(SidebarSubsection subSection) {
        Button button = new Button(subSection.getName());
        setButtonGraphic(button, subSection.getIconPath());
        button.getStyleClass().add(subSection.isSelected() ? "sidebar-subbutton-selected" : "sidebar-subbutton");
        subSection.getIsSelectedProperty().addListener((observable, oldValue, newValue) ->
            button.getStyleClass().setAll(Boolean.TRUE.equals(newValue) ? "sidebar-subbutton-selected" : "sidebar-subbutton"));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> subSection.getAction().run());

        return button;
    }

    private void toggleSection(VBox subSectionVBox, SidebarSection section) {
        section.setExpanded(!section.isExpanded());
        toggleNodeVisibility(subSectionVBox, section.isExpanded());
        Button correspondingToggleButton = toggleExpandSectionButtons.get(section.getName());
        if (correspondingToggleButton != null) {
            correspondingToggleButton.setGraphic(createImageView(section.isExpanded() ? caretUpIcon : caretDownIcon));
        }
    }

    // Sidebar toggling
    private void toggleSidebar() {
        if (isSidebarCollapsed) {
            expandSidebar();
        } else {
            collapseSidebar();
        }
        isSidebarCollapsed = !isSidebarCollapsed;
    }

    private void collapseSidebar() {
        // Reduce width and hide everything but the icons
        sidebar.setMinWidth(COLLAPSED_WIDTH);
        sidebar.setMaxWidth(COLLAPSED_WIDTH);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        dashboardHBox.getStyleClass().setAll("sidebar-title-container-collapsed");
        dashboardHBox.getChildren().forEach(child -> child.setVisible(false));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(false));
        toggleButton.setVisible(true);
        toggleButton.setManaged(true);

        navigationButtonContainer.getStyleClass().setAll("sidebar-inner-container-collapsed");
        for (int i = 0; i < navigationButtonContainer.getChildren().size(); i++) {
            Node node = navigationButtonContainer.getChildren().get(i);
            SidebarSection section = sections[i];
            if (node instanceof VBox) {
                toggleSectionVBox(section, false);
            }
        }
        bottomContainer.getStyleClass().setAll("sidebar-inner-container-collapsed");
        logoutButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    private void expandSidebar() {
        sidebar.setMinWidth(EXPANDED_WIDTH);
        sidebar.setMaxWidth(EXPANDED_WIDTH);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        dashboardHBox.getStyleClass().setAll("sidebar-title-container");
        dashboardHBox.getChildren().forEach(child -> child.setVisible(true));
        dashboardHBox.getChildren().forEach(child -> child.setManaged(true));

        navigationButtonContainer.getStyleClass().setAll("sidebar-inner-container");
        for (int i = 0; i < navigationButtonContainer.getChildren().size(); i++) {
            Node node = navigationButtonContainer.getChildren().get(i);
            SidebarSection section = sections[i];
            if (node instanceof VBox) {
                toggleSectionVBox(section, true);
            }
        }
        bottomContainer.getStyleClass().setAll("sidebar-inner-container");
        logoutButton.setContentDisplay(ContentDisplay.LEFT);
    }

    private void toggleSectionVBox(SidebarSection section, boolean isVisible) {
        for (Button mainNavigationButton : mainNavigationButtons) {
            Tooltip tooltip = new Tooltip(mainNavigationButton.getText());
            tooltip.getStyleClass().add("custom-tooltip");
            mainNavigationButton.setTooltip(!isVisible ? tooltip : null);
            mainNavigationButton.setContentDisplay(!isVisible ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        }
        for (Region sectionRegion : sectionRegions) {
            toggleNodeVisibility(sectionRegion, isVisible);
        }
        for (Button toggleExpandButton : toggleExpandSectionButtons.values()) {
            toggleNodeVisibility(toggleExpandButton, isVisible);
            toggleExpandButton.setGraphic(createImageView(isVisible ? caretDownIcon : caretUpIcon));
        }
        for (VBox subsectionVBox : subsectionVBoxes) {
            toggleNodeVisibility(subsectionVBox, isVisible && section.isExpanded());
        }
    }

    // Handle logout
    @FXML
    private void handleLogout() {
        authenticationService.logout();

        // Switch back to login scene
        try {
            SceneManager.loadLoginScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Utils
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

    private void updateScrollBarVisibility(ScrollPane scrollPane, Bounds contentBounds) {
        boolean verticalScrollNeeded = contentBounds.getHeight() > scrollPane.getViewportBounds().getHeight();

        String thumbStyle = verticalScrollNeeded ? "#d1d1d1" : "transparent";
        scrollPane.lookupAll(".thumb").forEach(thumb -> thumb.setStyle("-fx-background-color: " + thumbStyle));
    }
}
