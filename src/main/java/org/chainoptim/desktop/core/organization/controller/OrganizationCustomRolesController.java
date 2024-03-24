package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.FeaturePermissions;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.model.Permissions;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrganizationCustomRolesController implements DataReceiver<Organization> {

    private final CustomRoleService customRoleService;

    private final FallbackManager fallbackManager;

    private Organization organization;
    private List<CustomRole> customRoles;

    @FXML
    private GridPane customRolesPane;
    @FXML
    private List<Button> expandRoleButtons = new ArrayList<>();
    @FXML
    private List<Boolean> expandedRoles = new ArrayList<>();

    private static final String[] operations = {"Read", "Create", "Update", "Delete"};
    private static final String[] features = {"Products", "Factories", "Warehouses", "Suppliers", "Clients"};

    private Image angleUpImage;
    private Image angleDownImage;

    @Inject
    public OrganizationCustomRolesController(CustomRoleService customRoleService,
                                             FallbackManager fallbackManager) {
        this.customRoleService = customRoleService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;

        initializeUI();

        customRoleService.getCustomRolesByOrganizationId(organization.getId())
                .thenApply(this::handleCustomRolesResponse)
                .exceptionally(this::handleCustomRolesException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private void initializeUI() {
        angleUpImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));
    }

    private Optional<List<CustomRole>> handleCustomRolesResponse(Optional<List<CustomRole>> customRolesOptional) {
        Platform.runLater(() -> {
            if (customRolesOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load custom roles.");
                return;
            }
            customRoles = customRolesOptional.get();

            // Render the grid
            renderGridPane();
        });
        return customRolesOptional;
    }

    private Optional<List<CustomRole>> handleCustomRolesException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load custom roles."));
        return Optional.empty();
    }

    private void renderGridPane() {
        customRolesPane.getChildren().clear();

        customRolesPane.add(new Label("Custom Role"), 0, 0);

        // Headers
        for (int i = 0; i < operations.length; i++) {
            Label operationLabel = new Label(operations[i]);
            customRolesPane.add(operationLabel, i + 1, 0);
            GridPane.setHalignment(operationLabel, HPos.CENTER);
        }

        // Rows
        int rowIndex = 1; // Start from 1 to account for header row
        for (CustomRole role : customRoles) {
            // Render role row
            addRoleRow(role, rowIndex++);

            // Pre-allocate and hide feature permission rows
            for (String feature : features) {
                addFeaturePermissionRow(feature, role, rowIndex++);
            }
        }
    }

    private void addRoleRow(CustomRole role, int rowIndex) {
        Label roleNameLabel = new Label(role.getName());
        roleNameLabel.getStyleClass().add("parent-row");
        customRolesPane.add(roleNameLabel, 0, rowIndex);

        addExpandButton(role, rowIndex);

        // Add aggregate checkboxes
        for (int col = 0; col < operations.length; col++) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(aggregatePermissionsByIndex(role.getPermissions(), col)); // Select if all underlying permissions are true
            checkBox.setDisable(true);
            GridPane.setMargin(checkBox, new Insets(10, 40, 10, 40));
            customRolesPane.add(checkBox, col + 1, rowIndex);
        }
    }

    private void addExpandButton(CustomRole role, int rowIndex) {
        Button expandButton = new Button();
        expandButton.setGraphic(createImageView(angleDownImage));
        expandButton.getStyleClass().add("no-style-button");

        expandButton.setOnAction(event -> toggleFeaturePermissions(role, rowIndex));

        expandRoleButtons.add(expandButton);
        expandedRoles.add(false); // Start from collapsed state
        customRolesPane.add(expandButton, operations.length + 1, rowIndex);
    }

    private void addFeaturePermissionRow(String feature, CustomRole role, int rowIndex) {
        FeaturePermissions featurePermissions = getFeaturePermissionsByFeatureName(role.getPermissions(), feature);

        for (int col = 0; col <= operations.length; col++) {
            Node node;
            if (col == 0) {
                node = new Label(feature);
            } else {
                boolean hasPermission = getPermissionByIndex(featurePermissions, col - 1);
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(hasPermission);
                checkBox.setDisable(true);
                GridPane.setMargin(checkBox, new Insets(10, 40, 10, 40));
                node = checkBox;
            }

            node.setVisible(false);
            node.setManaged(false);
            customRolesPane.add(node, col, rowIndex);
            GridPane.setHalignment(node, HPos.CENTER);
        }
    }

    private void toggleFeaturePermissions(CustomRole role, int roleVisualRowIndex) {
        int roleIndex = customRoles.indexOf(role);
        boolean isExpanded = expandedRoles.get(roleIndex);

        int rowIndex = roleVisualRowIndex + 1;
        int featureRowsCount = features.length;

        for (int i = 0; i < featureRowsCount; i++, rowIndex++) {
            // Toggle visibility of each child in the feature permission rows
            toggleSubrowVisibility(rowIndex, !isExpanded);
        }
        // Toggle the expanded state
        expandedRoles.set(roleIndex, !isExpanded);
        expandRoleButtons.get(roleIndex).setGraphic(createImageView(isExpanded ? angleDownImage : angleUpImage));
    }

    private void toggleSubrowVisibility(int rowIndex, boolean isVisible) {
        customRolesPane.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == rowIndex)
                .forEach(node -> {
                    node.setVisible(isVisible);
                    node.setManaged(isVisible);
                });
    }

    private boolean aggregatePermissionsByIndex(Permissions permissions, int index) {
        return getPermissionByIndex(permissions.getProducts(), index) &&
                getPermissionByIndex(permissions.getFactories(), index) &&
                getPermissionByIndex(permissions.getWarehouses(), index) &&
                getPermissionByIndex(permissions.getSuppliers(), index) &&
                getPermissionByIndex(permissions.getClients(), index);
    }

    private boolean getPermissionByIndex(FeaturePermissions featurePermissions, int index) {
        if (featurePermissions == null) {
            return false;
        }

        return switch (index) {
            case 0 -> Optional.ofNullable(featurePermissions.getCanRead()).orElse(false);
            case 1 -> Optional.ofNullable(featurePermissions.getCanCreate()).orElse(false);
            case 2 -> Optional.ofNullable(featurePermissions.getCanUpdate()).orElse(false);
            case 3 -> Optional.ofNullable(featurePermissions.getCanDelete()).orElse(false);
            default -> false;
        };
    }

    private FeaturePermissions getFeaturePermissionsByFeatureName(Permissions permissions, String featureName) {
        return switch (featureName) {
            case "Products" -> permissions.getProducts();
            case "Factories" -> permissions.getFactories();
            case "Warehouses" -> permissions.getWarehouses();
            case "Suppliers" -> permissions.getSuppliers();
            case "Clients" -> permissions.getClients();
            default -> null;
        };
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }
}
