package org.chainoptim.desktop.features.production.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.production.dto.CreateFactoryDTO;
import org.chainoptim.desktop.features.production.model.Factory;
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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateFactoryController implements Initializable {

    // Services
    private final FactoryWriteService factoryWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;


    @Inject
    public CreateFactoryController(
            FactoryWriteService factoryWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager
    ) {
        this.factoryWriteService = factoryWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);

        initializeFormFields();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, null, "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateFactoryDTO factoryDTO = getCreateFactoryDTO(organizationId);
        if (factoryDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryWriteService.createFactory(factoryDTO)
                .thenApply(this::handleCreateFactoryResponse)
                .exceptionally(this::handleCreateClientException);
    }

    private Result<Factory> handleCreateFactoryResponse(Result<Factory> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);

            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create factory.", OperationOutcome.ERROR));
                return;
            }
            Factory factory = result.getData();
            toastManager.addToast(new ToastInfo(
                    "Success", "Factory created successfully.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(factory.getId());
            navigationService.switchView("Factory?id=" + factory.getId(), true, null);
        });
        return result;
    }

    private CreateFactoryDTO getCreateFactoryDTO(Integer organizationId) {
        CreateFactoryDTO factoryDTO = new CreateFactoryDTO();
        try {
            factoryDTO.setName(nameFormField.handleSubmit());
            factoryDTO.setOrganizationId(organizationId);
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

    private Result<Factory> handleCreateClientException(Throwable ex) {
        Platform.runLater(() ->
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create factory.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

