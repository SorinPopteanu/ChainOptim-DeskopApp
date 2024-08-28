package org.chainoptim.desktop.features.supply.supplier.controller;

import org.chainoptim.desktop.core.main.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.features.supply.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supply.supplier.model.Supplier;
import org.chainoptim.desktop.features.supply.supplier.service.SupplierWriteService;
import org.chainoptim.desktop.shared.common.ui.forms.FormField;
import org.chainoptim.desktop.shared.common.ui.forms.ValidationException;
import org.chainoptim.desktop.shared.common.ui.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateSupplierController implements Initializable {

    // Services
    private final SupplierWriteService supplierWriteService;
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
    public CreateSupplierController(
            SupplierWriteService supplierWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            ToastManager toastManager,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader) {
        this.supplierWriteService = supplierWriteService;
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
        nameFormField.initialize(String::new, "Name", true, null, "Your input is not valid.");
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateSupplierDTO supplierDTO = getCreateSupplierDTO(organizationId);
        if (supplierDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierWriteService.createSupplier(supplierDTO)
                .thenApply(this::handleCreateSupplierResponse)
                .exceptionally(this::handleCreateSupplierException);
    }

    private CreateSupplierDTO getCreateSupplierDTO(Integer organizationId) {
        CreateSupplierDTO supplierDTO = new CreateSupplierDTO();
        try {
            supplierDTO.setName(nameFormField.handleSubmit());
            supplierDTO.setOrganizationId(organizationId);
            if (selectOrCreateLocationController.isCreatingNewLocation()) {
                supplierDTO.setCreateLocation(true);
                supplierDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
            } else {
                supplierDTO.setCreateLocation(false);
                supplierDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
            }
        } catch (ValidationException e) {
            return null;
        }

        return supplierDTO;
    }

    private Result<Supplier> handleCreateSupplierResponse(Result<Supplier> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create supplier.", OperationOutcome.ERROR));
                return;
            }
            Supplier supplier = result.getData();
            toastManager.addToast(new ToastInfo(
                    "Success", "Supplier created successfully.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(supplier.getId());
            navigationService.switchView("Supplier?id=" + supplier.getId(), true, null);
        });
        return result;
    }

    private Result<Supplier> handleCreateSupplierException(Throwable ex) {
        Platform.runLater(() ->
            toastManager.addToast(new ToastInfo(
                    "Error", "Failed to create supplier.", OperationOutcome.ERROR)));
        return new Result<>();
    }

}

