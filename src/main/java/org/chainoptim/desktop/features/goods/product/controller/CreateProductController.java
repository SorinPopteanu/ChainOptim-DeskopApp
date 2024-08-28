package org.chainoptim.desktop.features.goods.product.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.goods.unit.model.UnitOfMeasurement;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.goods.product.service.ProductWriteService;
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

public class CreateProductController {

    private final ProductWriteService productWriteService;
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
    public CreateProductController(
            ProductWriteService productWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager
    ) {
        this.productWriteService = productWriteService;
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

        CreateProductDTO productDTO = getCreateProductDTO(organizationId);
        if (productDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        productWriteService.createProduct(productDTO)
                .thenApply(this::handleCreateProductResponse)
                .exceptionally(this::handleCreateProductException);
    }

    private CreateProductDTO getCreateProductDTO(Integer organizationId) {
        CreateProductDTO productDTO = new CreateProductDTO();
        productDTO.setOrganizationId(organizationId);
        try {
            productDTO.setName(nameFormField.handleSubmit());
            productDTO.setDescription(descriptionFormField.handleSubmit());
            UnitOfMeasurement newUnit = new UnitOfMeasurement(unitOfMeasurementSelect.getSelectedUnit(), unitOfMeasurementSelect.getSelectedMagnitude());
            productDTO.setNewUnit(newUnit);
        } catch (ValidationException e) {
            return null;
        }

        return productDTO;
    }

    private Result<Product> handleCreateProductResponse(Result<Product> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create product.", OperationOutcome.ERROR));
                return;
            }
            Product product = result.getData();
            fallbackManager.setLoading(false);
            toastManager.addToast(new ToastInfo
                    ("Product created.", "Product has been successfully created.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(product.getId());
            navigationService.switchView("Product?id=" + product.getId(), true, null);
        });
        return result;
    }

    private Result<Product> handleCreateProductException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to create product.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}
