package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.organization.dto.CreateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.dto.UpdateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.model.*;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrganizationCustomRolesController implements DataReceiver<Organization>, ConfirmUpdateDialogActionListener, ConfirmDeleteDialogActionListener {

    // Injected services and controllers
    private final CustomRoleService customRoleService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private ConfirmCustomRoleUpdateController confirmCustomRoleUpdateController;
    private ConfirmCustomRoleDeleteController confirmCustomRoleDeleteController;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button addNewRoleButton;
    @FXML
    private Button deleteRoleButton;
    @FXML
    private GridPane customRolesPane;
    @FXML
    private List<Button> expandRoleButtons = new ArrayList<>();
    @FXML
    private StackPane confirmUpdateDialogContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;

    // State
    private Organization organization;
    private List<CustomRole> customRoles;
    private final List<Boolean> expandedRoleStates = new ArrayList<>();
    private int currentEditedRowIndex = -1; // No edit marker
    private boolean isDeleteMode = false;
    private int currentToBeDeletedRowIndex = -1;

    // Constants
    private static final String[] operations = {"Read", "Create", "Update", "Delete"};
    private static final String[] features = {"Products", "Factories", "Warehouses", "Suppliers", "Clients"};

    // Icons
    private Image plusImage;
    private Image trashImage;
    private Image angleUpImage;
    private Image angleDownImage;
    private Image editImage;
    private Image saveImage;
    private Image cancelImage;

    @Inject
    public OrganizationCustomRolesController(CustomRoleService customRoleService,
                                             FXMLLoaderService fxmlLoaderService,
                                             ControllerFactory controllerFactory,
                                             FallbackManager fallbackManager) {
        this.customRoleService = customRoleService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;
        if (organization == null) {
            fallbackManager.setNoOrganization(true);
            return;
        }

        initializeIcons();
        loadConfirmUpdateDialog();
        loadConfirmDeleteDialog();

        loadCustomRoles();
    }

    // Initialize UI
    private void initializeIcons() {
        plusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
        trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        angleUpImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));
        editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
        saveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
        cancelImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
    }

    private void loadConfirmUpdateDialog() {
        // Load view into fallbackContainer
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/ConfirmCustomRoleUpdateView.fxml",
                controllerFactory::createController
        );
        try {
            Node confirmDialogView = loader.load();
            confirmCustomRoleUpdateController = loader.getController();
            confirmCustomRoleUpdateController.setConfirmUpdateDialogActionListener(this); // Listen to confirm dialog actions
            confirmUpdateDialogContainer.getChildren().add(confirmDialogView);
            closeConfirmUpdateDialog(); // Start hidden
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void loadConfirmDeleteDialog() {
        // Load view into fallbackContainer
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/ConfirmCustomRoleDeleteView.fxml",
                controllerFactory::createController
        );
        try {
            Node confirmDialogView = loader.load();
            confirmCustomRoleDeleteController = loader.getController();
            confirmCustomRoleDeleteController.setActionListener(this); // Listen to confirm dialog actions
            confirmDeleteDialogContainer.getChildren().add(confirmDialogView);
            closeConfirmDeleteDialog(); // Start hidden
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Load roles
    private void loadCustomRoles() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        customRoleService.getCustomRolesByOrganizationId(organization.getId())
                .thenApply(this::handleCustomRolesResponse)
                .exceptionally(this::handleCustomRolesException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<List<CustomRole>> handleCustomRolesResponse(Optional<List<CustomRole>> customRolesOptional) {
        Platform.runLater(() -> {
            if (customRolesOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load custom roles.");
                return;
            }
            customRoles = customRolesOptional.get();

            tabTitle.setText("Custom Roles (" + customRoles.size() + ")");
            styleDeleteRoleButton(deleteRoleButton);
            deleteRoleButton.setOnAction(event -> toggleDeleteMode());
            styleAddNewRoleButton(addNewRoleButton);
            addNewRoleButton.setOnAction(event -> handleAddNewRole());

            // Render the grid
            renderGridPane();
        });
        return customRolesOptional;
    }

    private Optional<List<CustomRole>> handleCustomRolesException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load custom roles."));
        return Optional.empty();
    }

    /*
     * Grid Layout:
     * C. Role 1 | Read | Create | Update | Delete | Expand | Edit/Cancel | Save | Delete
     * Feature 1 | Read | Create | Update | Delete
     * Feature 2 | Read | Create | Update | Delete
     * ...
     */
    private void renderGridPane() {
        customRolesPane.getChildren().clear();

        // Headers
        Label customRoleLabel = new Label("Role Name");
        customRoleLabel.getStyleClass().add("column-header");
        customRolesPane.add(customRoleLabel, 0, 0);

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
        addSaveButton(rowIndex);
        addDeleteButton(role, rowIndex);

        // Add aggregate checkboxes
        for (int col = 0; col < operations.length; col++) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(aggregatePermissionsByIndex(role.getPermissions(), col)); // Select if all the operation's permissions are true
            checkBox.setMouseTransparent(true);
            checkBox.setFocusTraversable(false);
            applyStandardMargin(checkBox);
            int finalCol = col;
            checkBox.setOnAction(event -> toggleOperationCheckboxes(rowIndex, finalCol, checkBox.isSelected())); // Toggle all the operation's permissions
            customRolesPane.add(checkBox, col + 1, rowIndex);
        }
    }

    private void toggleOperationCheckboxes(int rowIndex, int colIndex, boolean isSelected) {
        if (rowIndex != currentEditedRowIndex) return; // Only allow toggling when in edit mode

        for (int row = 1; row < features.length + 1; row++) {
            Node node = getNodeByRowColumnIndex(rowIndex + row, colIndex + 1);
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(isSelected);
            }
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
        System.out.println("Adding edit button to row " + rowIndex);
        Button editButton = new Button();
        styleEditButton(editButton);

        editButton.setOnAction(event -> toggleEditingRow(rowIndex, true));

        customRolesPane.add(editButton, operations.length + 2, rowIndex);
    }

    private void toggleEditingRow(int rowIndex, boolean isEdit) {
        if (isEdit) currentEditedRowIndex = rowIndex;

        // Toggle role name between Label and TextField
        toggleRoleName(rowIndex, isEdit);

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

        if (!isEdit) currentEditedRowIndex = -1;
    }

    private void toggleRoleName(int rowIndex, boolean isEdit) {
        Node roleNameNode = getNodeByRowColumnIndex(rowIndex, 0);
        String roleName = customRoles.get(getRoleIndexByRowIndex(currentEditedRowIndex)).getName();
        if (roleNameNode instanceof Label roleNameLabel && isEdit) {
            customRolesPane.getChildren().remove(roleNameLabel);
            customRolesPane.add(new TextField(roleName), 0, rowIndex);
        } else if (roleNameNode instanceof TextField roleNameField && !isEdit) {
            customRolesPane.getChildren().remove(roleNameField);
            Label roleNameLabel = new Label(roleName);
            roleNameLabel.getStyleClass().add("parent-row");
            customRolesPane.add(roleNameLabel, 0, rowIndex);
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
        System.out.println("Toggling edit button for row " + rowIndex);
        button.getStyleClass().clear();

        if (isEdit) {
            styleCancelButton(button);
            button.setOnAction(event -> {
                toggleEditingRow(rowIndex, false);
                refreshCheckboxSelections(rowIndex);
            });
        } else {
            styleEditButton(button);
            button.setOnAction(event -> toggleEditingRow(rowIndex, true));
        }
    }

    private void refreshCheckboxSelections(int rowIndex) {
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
    private void addSaveButton(int rowIndex) {
        Button saveButton = new Button();
        styleSaveButton(saveButton);
        saveButton.setVisible(false); // Start hidden since edit is false

        saveButton.setOnAction(event -> {
            if (currentEditedRowIndex == -1) return;
            CustomRole role = customRoles.get(getRoleIndexByRowIndex(currentEditedRowIndex));
            openConfirmUpdateDialog(role);
        });

        customRolesPane.add(saveButton, operations.length + 3, rowIndex);
    }

    @Override
    public void onConfirmCustomRoleUpdate(CustomRole customRole) {
        if (currentEditedRowIndex != -1) {
            saveRoleChanges(customRole, currentEditedRowIndex);
        } else {
            fallbackManager.setErrorMessage("No changes have been detected.");
            closeConfirmUpdateDialog();
        }
    }

    @Override
    public void onCancelCustomRoleUpdate() {
        closeConfirmUpdateDialog();
    }

    private void saveRoleChanges(CustomRole customRole, int rowIndex) {
        UpdateCustomRoleDTO updateCustomRoleDTO = gatherUpdatedCustomRole(customRole, rowIndex);
        System.out.println(updateCustomRoleDTO);

        // Save changes
        fallbackManager.setLoading(true);

        customRoleService.updateCustomRole(updateCustomRoleDTO)
                .thenAccept(updatedRole -> handleSuccessfulUpdate(updatedRole, rowIndex));
    }

    private UpdateCustomRoleDTO gatherUpdatedCustomRole(CustomRole customRole, int startingRowIndex) {
        // Get updated role name
        Node roleNameNode = getNodeByRowColumnIndex(startingRowIndex, 0);
        String roleName = roleNameNode instanceof TextField textField ? textField.getText() : customRole.getName();

        // Gather permissions
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

        return new UpdateCustomRoleDTO(customRole.getId(), roleName, permissions);
    }

    private void handleSuccessfulUpdate(Optional<CustomRole> updatedRoleOptional, int rowIndex) {
        Platform.runLater(() -> {
            if (updatedRoleOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to update custom role.");
                return;
            }
            CustomRole updatedRole = updatedRoleOptional.get();
            closeConfirmUpdateDialog();

            // Update customRoles
            customRoles.set(getRoleIndexByRowIndex(rowIndex), updatedRole);

            // Update UI
            refreshCheckboxSelections(rowIndex);

            // Disable editing row
            toggleEditingRow(rowIndex, false);

            // Hide save button
            Node node = getNodeByRowColumnIndex(rowIndex, operations.length + 3);
            if (node instanceof Button saveButton) {
                saveButton.setVisible(false);
            }

            fallbackManager.setLoading(false);
        });
    }

    // Delete
    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;

        for (int row = 1; row < customRoles.size() * (features.length + 1) + 1; row++) {
            Node node = getNodeByRowColumnIndex(row, operations.length + 4);
            if (node instanceof Button deleteButton) {
                deleteButton.setVisible(isDeleteMode);
            }
        }
    }

    private void addDeleteButton(CustomRole role, int rowIndex) {
        Button deleteButton = new Button();
        styleDeleteRoleButton(deleteButton);
        deleteButton.setOnAction(event -> {
            currentToBeDeletedRowIndex = rowIndex;
            openConfirmDeleteDialog(role.getId());
        });
        applyStandardMargin(deleteButton);
        deleteButton.setVisible(false);

        customRolesPane.add(deleteButton, operations.length + 4, rowIndex);
    }

    @Override
    public void onConfirmCustomRoleDelete(Integer customRoleId) {
        if (currentToBeDeletedRowIndex != -1) {
            handleDeleteRole(customRoleId, currentToBeDeletedRowIndex);
        } else {
            fallbackManager.setErrorMessage("No changes have been detected.");
            closeConfirmUpdateDialog();
        }
    }

    @Override
    public void onCancelCustomRoleDelete() {
        closeConfirmDeleteDialog();
    }

    private void handleDeleteRole(Integer customRoleId, int rowIndex) {
        fallbackManager.setLoading(true);

        customRoleService.deleteCustomRole(customRoleId)
                .thenAccept(deletedRoleIdOptional -> handleSuccessfulDelete(deletedRoleIdOptional, rowIndex));
    }

    private void handleSuccessfulDelete(Optional<Integer> deletedRoleIdOptional, int rowIndex) {
        Platform.runLater(() -> {
            if (deletedRoleIdOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to delete custom role.");
                return;
            }
            closeConfirmDeleteDialog();

            // Update customRoles
            customRoles.remove(getRoleIndexByRowIndex(rowIndex));

            // Update UI
            tabTitle.setText("Custom Roles (" + customRoles.size() + ")");

            // Rerender whole grid pane - necessary here, at least for rows after the deleted one
            customRolesPane.getChildren().clear();
            renderGridPane();

            fallbackManager.setLoading(false);
            toggleDeleteMode();
        });
    }

    // Create
    private void handleAddNewRole() {
        // Add new role
        String newRoleName = "New Role";
        Permissions newPermissions = new Permissions();
        CreateCustomRoleDTO roleDTO = new CreateCustomRoleDTO(newRoleName, organization.getId(), newPermissions);
        customRoleService.createCustomRole(roleDTO)
                .thenApply(this::handleNewRoleResponse)
                .exceptionally(ex -> {
                    Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to create new custom role."));
                    return Optional.empty();
                });
    }

    private Optional<CustomRole> handleNewRoleResponse(Optional<CustomRole> newRoleOptional) {
        Platform.runLater(() -> {
            if (newRoleOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to create new custom role.");
                return;
            }
            CustomRole newRole = newRoleOptional.get();
            customRoles.add(newRole);
            tabTitle.setText("Custom Roles (" + customRoles.size() + ")");

            // Render new role row and feature rows
            int newRowIndex = (customRoles.size() - 1) * (features.length + 1) + 1;
            addRoleRow(newRole, newRowIndex++);

            for (String feature : features) {
                addFeaturePermissionRow(feature, newRole, newRowIndex++);
            }
        });
        return newRoleOptional;
    }

    // Utils
    private void openConfirmUpdateDialog(CustomRole customRole) {
        confirmCustomRoleUpdateController.setData(customRole);
        confirmUpdateDialogContainer.setVisible(true);
        confirmUpdateDialogContainer.setManaged(true);
    }

    private void closeConfirmUpdateDialog() {
        confirmUpdateDialogContainer.setVisible(false);
        confirmUpdateDialogContainer.setManaged(false);
    }

    private void openConfirmDeleteDialog(Integer customRoleId) {
        confirmCustomRoleDeleteController.setData(customRoleId);
        confirmDeleteDialogContainer.setVisible(true);
        confirmDeleteDialogContainer.setManaged(true);
    }

    private void closeConfirmDeleteDialog() {
        confirmDeleteDialogContainer.setVisible(false);
        confirmDeleteDialogContainer.setManaged(false);
    }

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
    private void styleAddNewRoleButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(plusImage));
        button.setText("Add New Role");
        applyStandardMargin(button);
    }

    private void styleDeleteRoleButton(Button button) {
        button.getStyleClass().add("standard-delete-button");
        button.setGraphic(createImageView(trashImage));
        button.setTooltip(new Tooltip("Delete Role"));
        GridPane.setMargin(button, new Insets(12));
    }

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
        GridPane.setMargin(button, new Insets(12));
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
        GridPane.setMargin(node, new Insets(12, 36, 12, 36));
    }
}