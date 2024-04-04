package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductWriteService;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateUnitOfMeasurementController;
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

public class CreateProductController implements Initializable {

    private final ProductWriteService productWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
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
    public CreateProductController(
            ProductWriteService productWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader
    ) {
        this.productWriteService = productWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        unitOfMeasurementController = commonViewsLoader.loadSelectOrCreateUnitOfMeasurement(unitOfMeasurementContainer);
        unitOfMeasurementController.initialize();
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

        CreateProductDTO productDTO = getCreateProductDTO(organizationId);
        System.out.println("CreateProduct: " + productDTO.getUnitDTO());

        productWriteService.createProduct(productDTO)
                .thenAccept(productOptional ->
                    // Navigate to product page
                    Platform.runLater(() -> {
                        if (productOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create product.");
                            return;
                        }
                        Product product = productOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(product.getId());
                        navigationService.switchView("Product?id=" + product.getId(), true);
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateProductDTO getCreateProductDTO(Integer organizationId) {
        CreateProductDTO productDTO = new CreateProductDTO();
        productDTO.setOrganizationId(organizationId);
        productDTO.setName(nameField.getText());
        productDTO.setDescription(descriptionField.getText());
        if (unitOfMeasurementController.isCreatingNewUnit()) {
            productDTO.setCreateUnit(true);
            productDTO.setUnitDTO(unitOfMeasurementController.getNewUnitDTO());
        } else {
            productDTO.setCreateUnit(false);
            productDTO.setUnitId(unitOfMeasurementController.getSelectedUnit().getId());
        }

        return productDTO;
    }
}
