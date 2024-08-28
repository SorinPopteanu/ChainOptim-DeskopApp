package org.chainoptim.desktop.features.goods.component.controller;

import org.chainoptim.desktop.core.main.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.features.goods.unit.model.UnitOfMeasurement;
import org.chainoptim.desktop.features.goods.component.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.goods.component.service.ComponentService;
import org.chainoptim.desktop.features.goods.component.model.Component;
import org.chainoptim.desktop.shared.common.ui.forms.FormField;
import org.chainoptim.desktop.shared.common.ui.forms.ValidationException;
import org.chainoptim.desktop.shared.common.ui.select.SelectUnitOfMeasurement;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class CreateComponentController {

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
    public CreateComponentController(
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

        initializeFormFields();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, null, "Your input is not valid.");
        descriptionFormField.initialize(String::new, "Description", false, null, "Your input is not valid.");
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateComponentDTO componentDTO = getCreateComponentDTO(organizationId);
        if (componentDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        componentService.createComponent(componentDTO)
                .thenApply(this::handleCreateComponentResponse)
                .exceptionally(this::handleCreateComponentException);
    }

    private CreateComponentDTO getCreateComponentDTO(Integer organizationId) {
        CreateComponentDTO componentDTO = new CreateComponentDTO();
        componentDTO.setOrganizationId(organizationId);
        try {
            componentDTO.setName(nameFormField.handleSubmit());
            componentDTO.setDescription(descriptionFormField.handleSubmit());
            UnitOfMeasurement newUnit = new UnitOfMeasurement(unitOfMeasurementSelect.getSelectedUnit(), unitOfMeasurementSelect.getSelectedMagnitude());
            componentDTO.setNewUnit(newUnit);
        } catch (ValidationException e) {
            return null;
        }

        return componentDTO;
    }

    private Result<Component> handleCreateComponentResponse(Result<Component> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create component.", OperationOutcome.ERROR));
                return;
            }
            Component component = result.getData();
            fallbackManager.setLoading(false);
            toastManager.addToast(new ToastInfo
                    ("Component created.", "Component has been successfully created.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(component.getId());
            navigationService.switchView("Component?id=" + component.getId(), true, null);
        });
        return result;
    }

    private Result<Component> handleCreateComponentException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to create component.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}
