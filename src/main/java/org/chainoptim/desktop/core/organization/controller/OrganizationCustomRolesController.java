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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrganizationCustomRolesController implements DataReceiver<Organization> {

    private final CustomRoleService customRoleService;

    private final FallbackManager fallbackManager;

    private Organization organization;

    @FXML
    private GridPane customRolesPane;
    @FXML
    private List<Button> expandRoleButtons;
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
            List<CustomRole> customRoles = customRolesOptional.get();

            renderGridPane(customRoles);
        });
        return customRolesOptional;
    }

    private Optional<List<CustomRole>> handleCustomRolesException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load custom roles."));
        return Optional.empty();
    }

    private void renderGridPane(List<CustomRole> customRoles) {
        customRolesPane.getChildren().clear();
        customRolesPane.setHgap(40);
        customRolesPane.setVgap(40);

        customRolesPane.add(new Label("Custom Role"), 0, 0);

        // Headers
        for (int i = 0; i < operations.length; i++) {
            Label operationLabel = new Label(operations[i]);
            customRolesPane.add(operationLabel, i + 1, 0);
            GridPane.setHalignment(operationLabel, HPos.CENTER);
        }

        // Rows
        for (int row = 0; row < customRoles.size(); row++) {
            CustomRole role = customRoles.get(row);
            Label roleNameLabel = new Label(role.getName());
            roleNameLabel.getStyleClass().add("parent-row");
            customRolesPane.add(roleNameLabel, 0, row + 1);

            // Column values
            for (int col = 0; col < operations.length; col++) {
                CheckBox checkBox = new CheckBox();
                boolean hasPermission = aggregatePermissionsByIndex(role.getPermissions(), col);
                checkBox.setSelected(hasPermission);
                customRolesPane.add(checkBox, col + 1, row + 1);
                GridPane.setHalignment(checkBox, HPos.CENTER);
            }

            // Expand button
            expandedRoles.add(false);
            Button expandButton = new Button();
            expandButton.setGraphic(createImageView(angleDownImage));
            int finalRow = row;
            expandButton.setOnAction(event -> {
                if (Boolean.TRUE.equals(expandedRoles.get(finalRow))) {
                    expandedRoles.set(finalRow, false);
                    expandButton.setGraphic(createImageView(angleDownImage));
                } else {
                    expandedRoles.set(finalRow, true);
                    expandButton.setGraphic(createImageView(angleUpImage));
                }
            });
            customRolesPane.add(expandButton, operations.length + 1, row + 1);
        }
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

    private void renderRolePermissions(CustomRole role) {

    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }
}
