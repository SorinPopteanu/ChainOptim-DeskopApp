package org.chainoptim.desktop.features.productpipeline.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.productpipeline.service.ComponentService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateUnitOfMeasurementController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateComponentController implements Initializable {

    private final ComponentService componentService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private SelectOrCreateUnitOfMeasurementController unitOfMeasurementController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane unitOfMeasurementContainer;
    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;

    @Inject
    public CreateComponentController(
            ComponentService componentService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.componentService = componentService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectOrCreateUnitOfMeasurement();
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadSelectOrCreateUnitOfMeasurement() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateUnitOfMeasurementView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateUnitOfMeasurementView = loader.load();
            unitOfMeasurementController = loader.getController();
            unitOfMeasurementContainer.getChildren().add(selectOrCreateUnitOfMeasurementView);
            unitOfMeasurementController.initialize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateComponentDTO componentDTO = getCreateComponentDTO(organizationId);
        System.out.println("CreateComponent: " + componentDTO.getUnitDTO());

        componentService.createComponent(componentDTO)
                .thenAccept(componentOptional ->
                        // Navigate to component page
                        Platform.runLater(() -> {
                            if (componentOptional.isEmpty()) {
                                fallbackManager.setErrorMessage("Failed to create component.");
                                return;
                            }
                            Component component = componentOptional.get();
                            fallbackManager.setLoading(false);
                            currentSelectionService.setSelectedId(component.getId());
                            navigationService.switchView("Component?id=" + component.getId(), true);
                        })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateComponentDTO getCreateComponentDTO(Integer organizationId) {
        CreateComponentDTO componentDTO = new CreateComponentDTO();
        componentDTO.setOrganizationId(organizationId);
        componentDTO.setName(nameField.getText());
        componentDTO.setDescription(descriptionField.getText());
        if (unitOfMeasurementController.isCreatingNewUnit()) {
            componentDTO.setCreateUnit(true);
            componentDTO.setUnitDTO(unitOfMeasurementController.getNewUnitDTO());
        } else {
            componentDTO.setCreateUnit(false);
            componentDTO.setUnitId(unitOfMeasurementController.getSelectedUnit().getId());
        }

        return componentDTO;
    }
}
