package org.chainoptim.desktop.core.organization.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.service.OrganizationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    private final OrganizationService organizationService;

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

    @Inject
    public OrganizationController(OrganizationService organizationService,
                                  FallbackManager fallbackManager) {
        this.organizationService = organizationService;
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
                .exceptionally(this::handleOrganizationException)
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
        }
    }

    private Optional<Organization> handleOrganizationException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load organization."));
        return Optional.empty();
    }

    @FXML
    private void handleChangePlan() {
        System.out.println("Changing plan");
    }
}
