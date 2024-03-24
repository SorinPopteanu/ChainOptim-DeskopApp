package org.chainoptim.desktop.core.organization.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
import org.chainoptim.desktop.core.organization.service.OrganizationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    private final OrganizationService organizationService;
    private final CustomRoleService customRoleService;

    private final FallbackManager fallbackManager;

    private Organization organization;

    @FXML
    private Label organizationName;
    @FXML
    private Label organizationAddress;
    @FXML
    private Label planLabel;
    @FXML
    private VBox usersVBox;
    @FXML
    private VBox customRolesVBox;

    @Inject
    public OrganizationController(OrganizationService organizationService,
                                  CustomRoleService customRoleService,
                                  FallbackManager fallbackManager) {
        this.organizationService = organizationService;
        this.customRoleService = customRoleService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        organizationService.getOrganizationById(currentUser.getOrganization().getId(), true)
                .thenApply(this::handleOrganizationResponse)
                .exceptionally(this::handleOrganizationException);

        customRoleService.getCustomRolesByOrganizationId(currentUser.getOrganization().getId())
                .thenApply(this::handleCustomRolesResponse)
                .exceptionally(this::handleCustomRolesException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<Organization> handleOrganizationResponse(Optional<Organization> organizationOptional) {
        Platform.runLater(() -> {
            if (organizationOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load organization.");
                return;
            }
            usersVBox.getChildren().clear();
            organization = organizationOptional.get();

            initializeUI();
        });
        return organizationOptional;
    }

    private void initializeUI() {
        organizationName.setText("Organization: " + organization.getName());
        organizationAddress.setText("Address: " + organization.getAddress());
        planLabel.setText("Subscription Plan: " + organization.getSubscriptionPlan().name());
        System.out.println("Organization: " + organization);
        if (organization.getUsers() == null) {
            return;
        }

        for (User user : organization.getUsers()) {
            Label usernameLabel = new Label(user.getUsername());
            usersVBox.getChildren().add(usernameLabel);
            if (user.getCustomRole() != null) {
                Label customRoleLabel = new Label(user.getCustomRole().getName());
                usersVBox.getChildren().add(customRoleLabel);
            }
        }
    }

    private Optional<Organization> handleOrganizationException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load organization."));
        return Optional.empty();
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

    @FXML
    private void handleChangePlan() {
        System.out.println("Changing plan");
    }
}
