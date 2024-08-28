package org.chainoptim.desktop.features.supply.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.supply.dto.UpdateSupplierDTO;
import org.chainoptim.desktop.features.supply.model.Supplier;
import org.chainoptim.desktop.features.supply.service.SupplierService;
import org.chainoptim.desktop.features.supply.service.SupplierWriteService;
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

public class UpdateSupplierController implements Initializable {

    // Services
    private final SupplierService supplierService;
    private final SupplierWriteService supplierWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // State
    private Supplier supplier;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;

    @Inject
    public UpdateSupplierController(
            SupplierService supplierService,
            SupplierWriteService supplierWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager) {
        this.supplierService = supplierService;
        this.supplierWriteService = supplierWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        loadSupplier(currentSelectionService.getSelectedId());
    }

    private void loadSupplier(Integer supplierId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierService.getSupplierById(supplierId)
                .thenApply(this::handleSupplierResponse)
                .exceptionally(this::handleSupplierException);
    }

    private Result<Supplier> handleSupplierResponse(Result<Supplier> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load supplier.");
                return;
            }
            supplier = result.getData();
            fallbackManager.setLoading(false);

            initializeFormFields();
            selectOrCreateLocationController.setSelectedLocation(supplier.getLocation());
        });

        return result;
    }

    private Result<Supplier> handleSupplierException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load supplier."));
        return new Result<>();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, supplier.getName(), "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        UpdateSupplierDTO supplierDTO = getUpdateSupplierDTO();
        if (supplierDTO == null) return;
        System.out.println(supplierDTO);

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierWriteService.updateSupplier(supplierDTO)
                .thenApply(this::handleUpdateSupplierResponse)
                .exceptionally(this::handleUpdateSupplierException);
    }

    private UpdateSupplierDTO getUpdateSupplierDTO() {
        UpdateSupplierDTO supplierDTO = new UpdateSupplierDTO();
        supplierDTO.setId(supplier.getId());
        try {
            supplierDTO.setName(nameFormField.handleSubmit());

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

    private Result<Supplier> handleUpdateSupplierResponse(Result<Supplier> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update supplier.", OperationOutcome.ERROR));
                return;
            }
            toastManager.addToast(new ToastInfo(
                    "Success", "Supplier updated successfully.", OperationOutcome.SUCCESS));

            // Manage navigation, invalidating previous supplier cache
            Supplier updatedSupplier = result.getData();
            String supplierPage = "Supplier?id=" + updatedSupplier.getId();
            NavigationServiceImpl.invalidateViewCache(supplierPage);
            currentSelectionService.setSelectedId(updatedSupplier.getId());
            navigationService.switchView(supplierPage, true, null);
        });
        return result;
    }

    private Result<Supplier> handleUpdateSupplierException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update supplier.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

