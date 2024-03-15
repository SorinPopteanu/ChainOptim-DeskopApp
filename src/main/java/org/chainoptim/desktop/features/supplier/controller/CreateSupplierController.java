package org.chainoptim.desktop.features.supplier.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.supplier.dto.CreateSupplierDTO;
import org.chainoptim.desktop.features.supplier.model.Supplier;
import org.chainoptim.desktop.features.supplier.service.SupplierService;
import org.chainoptim.desktop.features.supplier.service.SupplierWriteService;
import org.chainoptim.desktop.features.supplier.service.SupplierWriteServiceImpl;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateSupplierController implements Initializable {

    private final SupplierWriteService supplierWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;


    @Inject
    public CreateSupplierController(
            SupplierWriteService supplierWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.supplierWriteService = supplierWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        fallbackManager.setLoading(true);

        Integer organizationId = currentUser.getOrganization().getId();

        CreateSupplierDTO supplierDTO = new CreateSupplierDTO();
        supplierDTO.setName(nameField.getText());
        supplierDTO.setOrganizationId(organizationId);

        System.out.println(supplierDTO);

        supplierWriteService.createSupplier(supplierDTO)
                .thenAccept(supplierOptional -> {
                    Platform.runLater(() -> {
                        if (supplierOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create supplier.");
                            return;
                        }
                        Supplier supplier = supplierOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(supplier.getId());
                        navigationService.switchView("Supplier?id=" + supplier.getId());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}

