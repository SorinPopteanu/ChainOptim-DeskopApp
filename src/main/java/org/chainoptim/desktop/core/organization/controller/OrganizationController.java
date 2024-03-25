package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.service.OrganizationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    private final OrganizationService organizationService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private final FallbackManager fallbackManager;

    private Organization organization;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab customRolesTab;
    @FXML
    private Tab subscriptionPlanTab;
    @FXML
    private Label organizationName;
    @FXML
    private Label organizationAddress;
    @FXML
    private Label planLabel;

    @Inject
    public OrganizationController(OrganizationService organizationService,
                                  FXMLLoaderService fxmlLoaderService,
                                  ControllerFactory controllerFactory,
                                  FallbackManager fallbackManager) {
        this.organizationService = organizationService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        loadFallbackManager();
        setupListeners();

        loadOrganization();
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                loadTabContent(overviewTab, "/org/chainoptim/desktop/core/organization/OrganizationOverviewView.fxml", this.organization);
            }
        });
        customRolesTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && customRolesTab.getContent() == null) {
                loadTabContent(customRolesTab, "/org/chainoptim/desktop/core/organization/OrganizationCustomRolesView.fxml", this.organization);
            }
        });
        subscriptionPlanTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && subscriptionPlanTab.getContent() == null) {
                loadTabContent(subscriptionPlanTab, "/org/chainoptim/desktop/core/organization/OrganizationSubscriptionPlanView.fxml", this.organization);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, Organization organization) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(controllerFactory::createController);
            Node content = loader.load();
            DataReceiver<Organization> controller = loader.getController();
            controller.setData(organization);
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOrganization() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        if (currentUser.getOrganization() == null) {
            Platform.runLater(() -> fallbackManager.setErrorMessage("You do not belong to any organization currently."));
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
            organization = organizationOptional.get();

            initializeUI();

            loadTabContent(overviewTab, "/org/chainoptim/desktop/core/organization/OrganizationOverviewView.fxml", this.organization);
        });
        return organizationOptional;
    }

    private void initializeUI() {
        organizationName.setText("Organization: " + organization.getName());
        organizationAddress.setText("Address: " + organization.getAddress());
        planLabel.setText("Subscription Plan: " + organization.getSubscriptionPlan().name());
        System.out.println("Organization: " + organization);
    }

    private Optional<Organization> handleOrganizationException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load organization."));
        return Optional.empty();
    }

    @FXML
    private void handleChangePlan() {

        loadTabContent(subscriptionPlanTab, "/org/chainoptim/desktop/core/organization/OrganizationSubscriptionPlanView.fxml", this.organization);
    }
}
