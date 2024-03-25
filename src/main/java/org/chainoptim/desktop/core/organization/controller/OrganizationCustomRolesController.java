package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.dto.UpdateCustomRoleDTO;
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

    private final List<Boolean> expandedRoleStates = new ArrayList<>();
    private final boolean editRoleState = false;

    private static final String[] operations = {"Read", "Create", "Update", "Delete"};
    private static final String[] features = {"Products", "Factories", "Warehouses", "Suppliers", "Clients"};

    private Image angleUpImage;
    private Image angleDownImage;
    private Image editImage;
    private Image saveImage;

    @Inject
    public OrganizationCustomRolesController(CustomRoleService customRoleService,
                                             FallbackManager fallbackManager) {
        this.customRoleService = customRoleService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        initializeIcons();

        customRoleService.getCustomRolesByOrganizationId(organization.getId())
                .thenApply(this::handleCustomRolesResponse)
                .exceptionally(this::handleCustomRolesException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private void initializeIcons() {
        angleUpImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));
        editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
        saveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
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

        Label customRoleLabel = new Label("Custom Role");
        customRoleLabel.getStyleClass().add("column-header");
        customRolesPane.add(customRoleLabel, 0, 0);

        // Headers
        for (int i = 0; i < operations.length; i++) {
            Label operationLabel = new Label(operations[i]);
            operationLabel.getStyleClass().add("column-header");
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
        addEditButton(rowIndex);
        addSaveButton(role, rowIndex);

        // Add aggregate checkboxes
        for (int col = 0; col < operations.length; col++) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(aggregatePermissionsByIndex(role.getPermissions(), col)); // Select if all underlying permissions are true
            checkBox.setMouseTransparent(true);
            checkBox.setFocusTraversable(false);
            applyStandardMargin(checkBox);
            customRolesPane.add(checkBox, col + 1, rowIndex);
        }
    }

    private void addExpandButton(CustomRole role, int rowIndex) {
        Button expandButton = new Button();
        expandButton.setGraphic(createImageView(angleDownImage));
        expandButton.getStyleClass().add("no-style-button");
        applyStandardMargin(expandButton);

        expandButton.setOnAction(event -> toggleFeaturePermissions(role, rowIndex));

        expandRoleButtons.add(expandButton);
        expandedRoleStates.add(false); // Start from collapsed state
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
                checkBox.setMouseTransparent(true);
                checkBox.setFocusTraversable(false);
                applyStandardMargin(checkBox);
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
        boolean isExpanded = expandedRoleStates.get(roleIndex);

        int rowIndex = roleVisualRowIndex + 1;
        int featureRowsCount = features.length;

        for (int i = 0; i < featureRowsCount; i++, rowIndex++) {
            // Toggle visibility of each child in the feature permission rows
            toggleSubrowVisibility(rowIndex, !isExpanded);
        }
        // Toggle the expanded state
        expandedRoleStates.set(roleIndex, !isExpanded);
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

    private void addEditButton(int rowIndex) {
        Button editButton = new Button();
        editButton.setGraphic(createImageView(editImage));
        editButton.getStyleClass().add("standard-write-button");

        editButton.setOnAction(event -> toggleEditingRow(rowIndex, true));

        customRolesPane.add(editButton, operations.length + 2, rowIndex);
    }

    private void toggleEditingRow(int roleVisualRowIndex, boolean isEdit) {
        if (editRoleState) return; // Only allow one row to be edited at a time

        // Enable row checkboxes
        toggleRowEdit(roleVisualRowIndex, isEdit);

        // Enable subrow checkboxes
        int rowIndex = roleVisualRowIndex + 1;
        int featureRowsCount = features.length;

        for (int i = 0; i < featureRowsCount; i++, rowIndex++) {
            toggleRowEdit(rowIndex, isEdit);
        }
    }

    private void toggleRowEdit(int rowIndex, boolean isEdit) {
        customRolesPane.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == rowIndex)
                .forEach(node -> {
                    if (node instanceof CheckBox checkBox) {
                        checkBox.setMouseTransparent(!isEdit);
                        checkBox.setFocusTraversable(isEdit);
                    }
                    if (node instanceof Button button) {
                        button.setVisible(isEdit); // Show save button
                    }
                });
    }

    private void addSaveButton(CustomRole customRole, int rowIndex) {
        Button saveButton = new Button();
        saveButton.setGraphic(createImageView(saveImage));
        saveButton.getStyleClass().add("standard-write-button");

        saveButton.setOnAction(event -> saveRoleChanges(customRole, rowIndex));

        saveButton.setVisible(false); // Start hidden since edit is false
        customRolesPane.add(saveButton, operations.length + 3, rowIndex);
    }

    private void saveRoleChanges(CustomRole customRole, int rowIndex) {
        UpdateCustomRoleDTO updateCustomRoleDTO = gatherRolePermissions(customRole, rowIndex);
        System.out.println(updateCustomRoleDTO);
    }

    private UpdateCustomRoleDTO gatherRolePermissions(CustomRole customRole, int startingRowIndex) {
        // Initialize a new Permissions object to hold the aggregated permissions.
        Permissions permissions = new Permissions();

        int startingFeatureIndex = startingRowIndex + 1;

        for (int i = 0; i < features.length; i++) {
            int currentRowIndex = startingFeatureIndex + i;

            FeaturePermissions featurePermissions = new FeaturePermissions();

            for (int col = 1; col <= operations.length; col++) {
                Node node = getNodeByRowColumnIndex(currentRowIndex, col, customRolesPane);

                if (!(node instanceof CheckBox checkBox)) {
                    continue;
                }
                boolean isSelected = checkBox.isSelected();

                // Assign permissions based on the column (operation)
                switch (col) {
                    case 1 -> featurePermissions.setCanRead(isSelected);
                    case 2 -> featurePermissions.setCanCreate(isSelected);
                    case 3 -> featurePermissions.setCanUpdate(isSelected);
                    case 4 -> featurePermissions.setCanDelete(isSelected);
                    default -> { break; }
                }
            }

            setFeaturePermissionsByFeatureName(permissions, features[i], featurePermissions);
        }

        return new UpdateCustomRoleDTO(customRole.getId(), customRole.getName(), permissions);
    }

    // Utils
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

    private void setFeaturePermissionsByFeatureName(Permissions permissions, String featureName, FeaturePermissions featurePermissions) {
        switch (featureName) {
            case "Products" -> permissions.setProducts(featurePermissions);
            case "Factories" -> permissions.setFactories(featurePermissions);
            case "Warehouses" -> permissions.setWarehouses(featurePermissions);
            case "Suppliers" -> permissions.setSuppliers(featurePermissions);
            case "Clients" -> permissions.setClients(featurePermissions);
            default -> { break; }
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        for (Node node : gridPane.getChildren()) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void applyStandardMargin(Node node) {
        GridPane.setMargin(node, new Insets(10, 40, 10, 40));
    }
}
