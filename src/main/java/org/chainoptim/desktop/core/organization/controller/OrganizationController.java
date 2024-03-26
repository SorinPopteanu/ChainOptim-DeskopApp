package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.organization.service.CustomRoleService;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrganizationController implements Initializable {

    // Services
    private final OrganizationService organizationService;
    private final CustomRoleService customRoleService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private final FallbackManager fallbackManager;

    // Controllers
    private OrganizationOverviewController organizationOverviewController;

    // State
    private Integer organizationId;
    private OrganizationViewData organizationViewData;

    // FXML
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
                                    CustomRoleService customRoleService,
                                  FXMLLoaderService fxmlLoaderService,
                                  ControllerFactory controllerFactory,
                                  FallbackManager fallbackManager) {
        this.organizationService = organizationService;
        this.customRoleService = customRoleService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        loadFallbackManager();
        setupListeners();
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        if (currentUser.getOrganization() == null) {
            Platform.runLater(() -> fallbackManager.setErrorMessage("You do not belong to any organization currently."));
            return;
        }
        organizationId = currentUser.getOrganization().getId();

        organizationViewData = new OrganizationViewData();
        loadOrganization();
        loadCustomRoles(); // Multi-thread this as custom roles are not immediately needed
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
                loadTabContent(overviewTab, "/org/chainoptim/desktop/core/organization/OrganizationOverviewView.fxml", this.organizationViewData);
            }
        });
        customRolesTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && customRolesTab.getContent() == null) {
                loadTabContent(customRolesTab, "/org/chainoptim/desktop/core/organization/OrganizationCustomRolesView.fxml", this.organizationViewData);
            }
        });
        subscriptionPlanTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && subscriptionPlanTab.getContent() == null) {
                loadTabContent(subscriptionPlanTab, "/org/chainoptim/desktop/core/organization/OrganizationSubscriptionPlanView.fxml", this.organizationViewData);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadTabContent(Tab tab, String fxmlFilepath, OrganizationViewData organizationViewData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFilepath));
            loader.setControllerFactory(controllerFactory::createController);
            Node content = loader.load();
            DataReceiver<OrganizationViewData> controller = loader.getController();
            if (tab == overviewTab) {
                organizationOverviewController = (OrganizationOverviewController) controller;
            }
            controller.setData(organizationViewData);
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOrganization() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        organizationService.getOrganizationById(organizationId, true)
                .thenApply(this::handleOrganizationResponse)
                .exceptionally(this::handleOrganizationException);
    }

    private Optional<Organization> handleOrganizationResponse(Optional<Organization> organizationOptional) {
        Platform.runLater(() -> {
            if (organizationOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load organization.");
                return;
            }
            organizationViewData.setOrganization(organizationOptional.get());
            System.out.println("Organization: " + organizationViewData.getOrganization());

            initializeUI();

            loadTabContent(overviewTab, "/org/chainoptim/desktop/core/organization/OrganizationOverviewView.fxml", this.organizationViewData);
            // Hide loading screen even if custom roles are not loaded yet,
            // as they are not immediately needed
            fallbackManager.setLoading(false);
        });

        return organizationOptional;
    }

    private void loadCustomRoles() {
        customRoleService.getCustomRolesByOrganizationId(organizationId)
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
            organizationViewData.setCustomRoles(customRolesOptional.get());
            organizationOverviewController.setData(organizationViewData);
            System.out.println("Custom Roles: " + organizationViewData.getCustomRoles());
        });
        return customRolesOptional;
    }

    private Optional<List<CustomRole>> handleCustomRolesException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load custom roles."));
        return Optional.empty();
    }

    private void initializeUI() {
        organizationName.setText("Organization: " + organizationViewData.getOrganization().getName());
        organizationAddress.setText("Address: " + organizationViewData.getOrganization().getAddress());
        planLabel.setText("Subscription Plan: " + organizationViewData.getOrganization().getSubscriptionPlan().name());
        System.out.println("Organization: " + organizationViewData.getOrganization());
    }

    private Optional<Organization> handleOrganizationException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load organization."));
        return Optional.empty();
    }

    @FXML
    private void handleEditOrganization() {
        System.out.println("Edit organization clicked");
    }
}
