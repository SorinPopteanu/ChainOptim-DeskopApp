package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryWriteService;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateLocationController;
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

public class CreateFactoryController implements Initializable {

    private final FactoryWriteService factoryWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;


    @Inject
    public CreateFactoryController(
            FactoryWriteService factoryWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.factoryWriteService = factoryWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectOrCreateLocation();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadSelectOrCreateLocation() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateLocationView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateLocationView = loader.load();
            selectOrCreateLocationController = loader.getController();
            selectOrCreateLocationContainer.getChildren().add(selectOrCreateLocationView);
            selectOrCreateLocationController.initialize();
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

        CreateFactoryDTO factoryDTO = getCreateFactoryDTO(organizationId);

        System.out.println(factoryDTO);

        factoryWriteService.createFactory(factoryDTO)
                .thenAccept(factoryOptional ->
                    Platform.runLater(() -> {
                        if (factoryOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create factory.");
                            return;
                        }
                        Factory factory = factoryOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(factory.getId());
                        navigationService.switchView("Factory?id=" + factory.getId());
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateFactoryDTO getCreateFactoryDTO(Integer organizationId) {
        CreateFactoryDTO factoryDTO = new CreateFactoryDTO();
        factoryDTO.setName(nameField.getText());
        factoryDTO.setOrganizationId(organizationId);
        if (selectOrCreateLocationController.isCreatingNewLocation()) {
            factoryDTO.setCreateLocation(true);
            factoryDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
        } else {
            factoryDTO.setCreateLocation(false);
            factoryDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
        }

        return factoryDTO;
    }
}

