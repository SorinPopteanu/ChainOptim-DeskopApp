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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

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
    private boolean editRoleState = false;

    private static final String[] operations = {"Read", "Create", "Update", "Delete"};
    private static final String[] features = {"Products", "Factories", "Warehouses", "Suppliers", "Clients"};

    private Image angleUpImage;
    private Image angleDownImage;
    private Image editImage;
    private Image saveImage;
    private Image cancelImage;

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
        cancelImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
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
        // Add row name label
        Label roleNameLabel = new Label(role.getName());
        roleNameLabel.getStyleClass().add("parent-row");
        customRolesPane.add(roleNameLabel, 0, rowIndex);

        // Add row buttons
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

    private void addFeaturePermissionRow(String feature, CustomRole role, int rowIndex) {
        FeaturePermissions featurePermissions = getFeaturePermissionsByFeatureName(role.getPermissions(), feature);

        for (int col = 0; col <= operations.length; col++) {
            Node node;
            if (col == 0) {
                node = new Label(feature);
            } else {
                boolean hasPermission = getPermissionByColumnIndex(featurePermissions, col - 1);
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

    // Buttons and their actions
    // - Expand
    private void addExpandButton(CustomRole role, int rowIndex) {
        Button expandButton = new Button();
        styleExpandButton(expandButton);

        expandButton.setOnAction(event -> toggleFeaturePermissions(role, rowIndex));

        expandRoleButtons.add(expandButton);
        expandedRoleStates.add(false); // Start from collapsed state
        customRolesPane.add(expandButton, operations.length + 1, rowIndex);
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

    // - Edit
    private void addEditButton(int rowIndex) {
        Button editButton = new Button();
        styleEditButton(editButton);

        editButton.setOnAction(event -> toggleEditingRow(rowIndex, true));

        customRolesPane.add(editButton, operations.length + 2, rowIndex);
    }

    private void toggleEditingRow(int rowIndex, boolean isEdit) {
        editRoleState = isEdit;

        // Enable row checkboxes
        toggleRowEdit(rowIndex, isEdit);

        // Enable subrow checkboxes
        int featureRowIndex = rowIndex + 1;
        int featureRowsCount = features.length;

        for (int i = 0; i < featureRowsCount; i++, featureRowIndex++) {
            toggleRowEdit(featureRowIndex, isEdit);
        }

        // Hide all edit buttons except the one being edited, show cancel button
        for (int row = 0; row < customRoles.size(); row++) {
            int realIndex = row * (featureRowsCount + 1) + 1;
            Node editButtonNode = getNodeByRowColumnIndex(realIndex, operations.length + 2);

            if (editButtonNode instanceof Button editButton) {
                if (realIndex == rowIndex) {
                    toggleEditCancelButton(editButton, rowIndex, isEdit);
                } else {
                    editButton.setVisible(!isEdit);
                }
            }
        }

        // Show save button in edit mode
        Node saveButtonNode = getNodeByRowColumnIndex(rowIndex, operations.length + 3);
        if (saveButtonNode instanceof Button saveButton) {
            saveButton.setVisible(isEdit);
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
                });
    }

    private void toggleEditCancelButton(Button button, int rowIndex, boolean isEdit) {
        button.getStyleClass().clear();

        if (isEdit) {
            styleCancelButton(button);
            button.setOnAction(event -> {
                toggleEditingRow(rowIndex, false);
                cancelCheckboxSelections(rowIndex);
            });
        } else {
            styleEditButton(button);
            button.setOnAction(event -> toggleEditingRow(rowIndex, true));
        }
    }

    private void cancelCheckboxSelections(int rowIndex) {
        // Reselect based on the original permissions
        int startingFeatureIndex = rowIndex + 1;
        Permissions permissions = customRoles.get(getRoleIndexByRowIndex(rowIndex)).getPermissions();

        // Row checkboxes
        for (int col = 1; col <= operations.length; col++) {
            Node node = getNodeByRowColumnIndex(rowIndex, col);
            if (!(node instanceof CheckBox checkBox)) {
                continue;
            }
            checkBox.setSelected(aggregatePermissionsByIndex(permissions, col - 1));
        }

        // Feature checkboxes
        for (int i = 0; i < features.length; i++) {
            int currentRowIndex = startingFeatureIndex + i;
            FeaturePermissions featurePermissions = getFeaturePermissionsByFeatureName(permissions, features[i]);

            for (int col = 1; col <= operations.length; col++) {
                Node node = getNodeByRowColumnIndex(currentRowIndex, col);

                if (!(node instanceof CheckBox checkBox)) {
                    continue;
                }

                boolean hasPermission = getPermissionByColumnIndex(featurePermissions, col - 1);
                checkBox.setSelected(hasPermission);
            }
        }
    }

    // - Save
    private void addSaveButton(CustomRole customRole, int rowIndex) {
        Button saveButton = new Button();
        styleSaveButton(saveButton);

        saveButton.setOnAction(event -> saveRoleChanges(customRole, rowIndex));

        saveButton.setVisible(false); // Start hidden since edit is false
        customRolesPane.add(saveButton, operations.length + 3, rowIndex);
    }

    private void saveRoleChanges(CustomRole customRole, int rowIndex) {
        UpdateCustomRoleDTO updateCustomRoleDTO = gatherRolePermissions(customRole, rowIndex);
        System.out.println(updateCustomRoleDTO);

        // Save changes
        fallbackManager.setLoading(true);

        customRoleService.updateCustomRole(updateCustomRoleDTO)
                .thenAccept(updatedRole -> handleSuccessfulUpdate(updatedRole, rowIndex));
    }

    private UpdateCustomRoleDTO gatherRolePermissions(CustomRole customRole, int startingRowIndex) {
        Permissions permissions = new Permissions();

        int startingFeatureIndex = startingRowIndex + 1;

        for (int i = 0; i < features.length; i++) {
            int currentRowIndex = startingFeatureIndex + i;

            FeaturePermissions featurePermissions = new FeaturePermissions();

            for (int col = 1; col <= operations.length; col++) {
                Node node = getNodeByRowColumnIndex(currentRowIndex, col);

                if (!(node instanceof CheckBox checkBox)) {
                    continue;
                }
                boolean isSelected = checkBox.isSelected();

                // Assign permissions based on the column (operation)
                setPermissionsByIndex(featurePermissions, col - 1, isSelected);
            }

            setFeaturePermissionsByFeatureName(permissions, features[i], featurePermissions);
        }

        return new UpdateCustomRoleDTO(customRole.getId(), customRole.getName(), permissions);
    }

    private void handleSuccessfulUpdate(Optional<CustomRole> updatedRoleOptional, int rowIndex) {
        Platform.runLater(() -> {
            if (updatedRoleOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to update custom role.");
                return;
            }

            fallbackManager.setLoading(false);

            // Disable editing row
            toggleEditingRow(rowIndex, false);

            // Hide save button
            Node node = getNodeByRowColumnIndex(rowIndex, operations.length + 3);
            if (node instanceof Button saveButton) {
                saveButton.setVisible(false);
            }
        });
    }

    // Utils
    private boolean aggregatePermissionsByIndex(Permissions permissions, int index) {
        return getPermissionByColumnIndex(permissions.getProducts(), index) &&
                getPermissionByColumnIndex(permissions.getFactories(), index) &&
                getPermissionByColumnIndex(permissions.getWarehouses(), index) &&
                getPermissionByColumnIndex(permissions.getSuppliers(), index) &&
                getPermissionByColumnIndex(permissions.getClients(), index);
    }

    private boolean getPermissionByColumnIndex(FeaturePermissions featurePermissions, int columnIndex) {
        if (featurePermissions == null) {
            return false;
        }

        return switch (columnIndex) {
            case 0 -> Optional.ofNullable(featurePermissions.getCanRead()).orElse(false);
            case 1 -> Optional.ofNullable(featurePermissions.getCanCreate()).orElse(false);
            case 2 -> Optional.ofNullable(featurePermissions.getCanUpdate()).orElse(false);
            case 3 -> Optional.ofNullable(featurePermissions.getCanDelete()).orElse(false);
            default -> false;
        };
    }

    private int getRoleIndexByRowIndex(int rowIndex) {
        return rowIndex / (features.length + 1);
    }

    private void setPermissionsByIndex(FeaturePermissions featurePermissions, int index, boolean value) {
        switch (index) {
            case 0 -> featurePermissions.setCanRead(value);
            case 1 -> featurePermissions.setCanCreate(value);
            case 2 -> featurePermissions.setCanUpdate(value);
            case 3 -> featurePermissions.setCanDelete(value);
            default -> { break; }
        }
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

    private Node getNodeByRowColumnIndex(final int row, final int column) {
        Node result = null;
        for (Node node : customRolesPane.getChildren()) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    // Button styling
    private void styleExpandButton(Button button) {
        button.getStyleClass().add("no-style-button");
        button.setGraphic(createImageView(angleDownImage));
        button.setTooltip(new Tooltip("Expand"));
        applyStandardMargin(button);
    }

    private void styleEditButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(editImage));
        button.setTooltip(new Tooltip("Edit Role"));
    }

    private void styleSaveButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(saveImage));
        button.setTooltip(new Tooltip("Save Changes"));
        GridPane.setMargin(button, new Insets(10));
    }

    private void styleCancelButton(Button button) {
        button.getStyleClass().add("cancel-edit-button");
        button.setGraphic(createImageView(cancelImage));
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
