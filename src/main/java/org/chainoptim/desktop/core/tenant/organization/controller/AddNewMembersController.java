package org.chainoptim.desktop.core.tenant.organization.controller;

import org.chainoptim.desktop.core.main.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.tenant.organization.service.OrganizationService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AddNewMembersController implements Initializable {

    private final OrganizationService organizationService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Integer organizationId;

    @FXML
    private StackPane usersSelectionContainer;
    @FXML
    private StackPane fallbackContainer;

    @Inject
    public AddNewMembersController(OrganizationService organizationService,
                                    NavigationService navigationService,
                                    CurrentSelectionService currentSelectionService,
                                    FXMLLoaderService fxmlLoaderService,
                                    ControllerFactory controllerFactory,
                                    FallbackManager fallbackManager) {
        this.organizationService = organizationService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        loadFallbackManager();
        loadUsersSearch();
        setupListeners();

        Integer receivedOrganizationId = currentSelectionService.getSelectedId();
        if (receivedOrganizationId != null) {
            System.out.println("Organization ID: " + receivedOrganizationId);
            this.organizationId = receivedOrganizationId;
        } else {
            fallbackManager.setErrorMessage("Failed to load organization.");
        }
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadUsersSearch() {
        Node usersSearchView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/core/user/PublicUsersSearchAndSelectionView.fxml",
                controllerFactory::createController
        );
        usersSelectionContainer.getChildren().add(usersSearchView);
    }

    private void setupListeners() {
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            usersSelectionContainer.setVisible(newValue);
            usersSelectionContainer.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }
}
