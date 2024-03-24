package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class OrganizationCustomRolesController implements DataReceiver<Organization> {

    private final CustomRoleService customRoleService;

    private final FallbackManager fallbackManager;

    private Organization organization;

    @FXML
    private VBox customRolesVBox;

    @Inject
    public OrganizationCustomRolesController(CustomRoleService customRoleService,
                                             FallbackManager fallbackManager) {
        this.customRoleService = customRoleService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;

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
            customRolesVBox.getChildren().clear();
            List<CustomRole> customRoles = customRolesOptional.get();

            for (CustomRole customRole : customRoles) {
                Label customRoleLabel = new Label(customRole.getName());
                customRolesVBox.getChildren().add(customRoleLabel);
            }
        });
        return customRolesOptional;
    }

    private Optional<List<CustomRole>> handleCustomRolesException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load custom roles."));
        return Optional.empty();
    }
}
