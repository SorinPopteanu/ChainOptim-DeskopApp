package org.chainoptim.desktop.features.production.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.production.dto.UpdateFactoryDTO;
import org.chainoptim.desktop.features.production.model.Factory;
import org.chainoptim.desktop.features.production.service.FactoryService;
import org.chainoptim.desktop.features.production.service.FactoryWriteService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateFactoryController implements Initializable {

    // Services
    private final FactoryService factoryService;
    private final FactoryWriteService factoryWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // State
    private Factory factory;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;

    @Inject
    public UpdateFactoryController(
            FactoryService factoryService,
            FactoryWriteService factoryWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager
    ) {
        this.factoryService = factoryService;
        this.factoryWriteService = factoryWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.toastManager = toastManager;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        loadFactory(currentSelectionService.getSelectedId());
    }

    private void loadFactory(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryService.getFactoryById(factoryId)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException);
    }

    private Result<Factory> handleFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load factory.");
                return;
            }
            factory = result.getData();
            fallbackManager.setLoading(false);

            initializeFormFields();
            selectOrCreateLocationController.setSelectedLocation(factory.getLocation());
        });
        return result;
    }

    private Result<Factory> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factory."));
        return new Result<>();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, factory.getName(), "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        UpdateFactoryDTO factoryDTO = getUpdateFactoryDTO();
        if (factoryDTO == null) return;
        System.out.println(factoryDTO);

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryWriteService.updateFactory(factoryDTO)
                .thenApply(this::handleUpdateFactoryResponse)
                .exceptionally(this::handleUpdateFactoryException);
    }

    private UpdateFactoryDTO getUpdateFactoryDTO() {
        UpdateFactoryDTO factoryDTO = new UpdateFactoryDTO();
        try {
            factoryDTO.setId(factory.getId());
            factoryDTO.setName(nameFormField.handleSubmit());

            if (selectOrCreateLocationController.isCreatingNewLocation()) {
                factoryDTO.setCreateLocation(true);
                factoryDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
            } else {
                factoryDTO.setCreateLocation(false);
                factoryDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
            }
        } catch (ValidationException e) {
            return null;
        }

        return factoryDTO;
    }

    private Result<Factory> handleUpdateFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update factory.", OperationOutcome.ERROR));
                return;
            }
            toastManager.addToast(new ToastInfo(
                    "Success", "Factory updated successfully.", OperationOutcome.SUCCESS));

            // Manage navigation, invalidating previous factory cache
            Factory updatedFactory = result.getData();
            String factoryPage = "Factory?id=" + updatedFactory.getId();
            NavigationServiceImpl.invalidateViewCache(factoryPage);
            currentSelectionService.setSelectedId(updatedFactory.getId());
            navigationService.switchView(factoryPage, true, null);
        });
        return result;
    }

    private Result<Factory> handleUpdateFactoryException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update factory.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

