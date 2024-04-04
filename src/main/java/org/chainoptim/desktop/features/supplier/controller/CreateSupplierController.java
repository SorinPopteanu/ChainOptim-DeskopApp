package org.chainoptim.desktop.features.supplier.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierWriteService;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateSupplierController implements Initializable {

    private final SupplierWriteService supplierWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;

    @Inject
    public CreateSupplierController(
            SupplierWriteService supplierWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader
    ) {
        this.supplierWriteService = supplierWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
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

        CreateSupplierDTO supplierDTO = getCreateSupplierDTO(organizationId);

        supplierWriteService.createSupplier(supplierDTO)
                .thenAccept(supplierOptional ->
                    Platform.runLater(() -> {
                        if (supplierOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create supplier.");
                            return;
                        }
                        Supplier supplier = supplierOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(supplier.getId());
                        navigationService.switchView("Supplier?id=" + supplier.getId(), true);
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateSupplierDTO getCreateSupplierDTO(Integer organizationId) {
        CreateSupplierDTO supplierDTO = new CreateSupplierDTO();
        supplierDTO.setOrganizationId(organizationId);
        supplierDTO.setName(nameField.getText());
        if (selectOrCreateLocationController.isCreatingNewLocation()) {
            supplierDTO.setCreateLocation(true);
            supplierDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
        } else {
            supplierDTO.setCreateLocation(false);
            supplierDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
        }

        return supplierDTO;
    }
}

