package org.chainoptim.desktop.features.productpipeline.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateComponentDTO;
import org.chainoptim.desktop.features.product.model.NewUnitOfMeasurement;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.productpipeline.service.ComponentService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.common.uielements.select.SelectUnitOfMeasurement;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class UpdateComponentController {

    private final ComponentService componentService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane unitOfMeasurementContainer;
    @FXML
    private FormField<String> nameFormField;
    @FXML
    private FormField<String> descriptionFormField;
    @FXML
    private SelectUnitOfMeasurement unitOfMeasurementSelect;

    @Inject
    public UpdateComponentController(
            ComponentService componentService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager
    ) {
        this.componentService = componentService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    public void initialize() {
        commonViewsLoader.loadFallbackManager(fallbackContainer);

        loadComponent(currentSelectionService.getSelectedId());
    }

    private void loadComponent(Integer componentId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        componentService.getComponentById(componentId)
                .thenApply(this::handleComponentResponse)
                .exceptionally(this::handleComponentException);
    }

    private Result<Component> handleComponentResponse(Result<Component> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load component");
                return;
            }

            Component component = result.getData();
            initializeFormFields(component);
            fallbackManager.setLoading(false);
        });
        return result;
    }

    private Result<Component> handleComponentException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load component."));
        return new Result<>();
    }

    private void initializeFormFields(Component component) {
        nameFormField.initialize(String::new, "Name", true, component.getName(), "Your input is not valid.");
        descriptionFormField.initialize(String::new,"Description", false, component.getDescription(), "Your input is not valid.");
        if (component.getNewUnit() != null) {
            unitOfMeasurementSelect.initialize(component.getNewUnit().getStandardUnit(), component.getNewUnit().getUnitMagnitude());
        }
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        UpdateComponentDTO componentDTO = getUpdateComponentDTO(organizationId);
        if (componentDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        componentService.updateComponent(componentDTO)
                .thenApply(this::handleUpdateComponentResponse)
                .exceptionally(this::handleUpdateComponentException);
    }

    private UpdateComponentDTO getUpdateComponentDTO(Integer organizationId) {
        UpdateComponentDTO componentDTO = new UpdateComponentDTO();
        componentDTO.setId(currentSelectionService.getSelectedId());
        componentDTO.setOrganizationId(organizationId);
        try {
            componentDTO.setName(nameFormField.handleSubmit());
            componentDTO.setDescription(descriptionFormField.handleSubmit());
            NewUnitOfMeasurement newUnit = new NewUnitOfMeasurement(unitOfMeasurementSelect.getSelectedUnit(), unitOfMeasurementSelect.getSelectedMagnitude());
            componentDTO.setNewUnit(newUnit);
        } catch (ValidationException e) {
            return null;
        }

        return componentDTO;
    }

    private Result<Component> handleUpdateComponentResponse(Result<Component> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update component.", OperationOutcome.ERROR));
                return;
            }
            Component component = result.getData();
            fallbackManager.setLoading(false);
            toastManager.addToast(new ToastInfo
                    ("Component updated.", "Component has been successfully updated.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(component.getId());
            navigationService.switchView("Component?id=" + component.getId(), true, null);
        });
        return result;
    }

    private Result<Component> handleUpdateComponentException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update component.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}
