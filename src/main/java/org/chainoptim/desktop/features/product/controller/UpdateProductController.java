package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.dto.UpdateProductDTO;
import org.chainoptim.desktop.features.product.model.NewUnitOfMeasurement;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.features.product.service.ProductWriteService;
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

public class UpdateProductController {

    private final ProductService productService;
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
    public UpdateProductController(
            ProductService productService,
            ProductWriteService productWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager
    ) {
        this.productService = productService;
        this.productWriteService = productWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    public void initialize() {
        commonViewsLoader.loadFallbackManager(fallbackContainer);

        loadProduct(currentSelectionService.getSelectedId());
    }

    private void loadProduct(Integer productId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        productService.getProductWithStages(productId)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException);
    }

    private Result<Product> handleProductResponse(Result<Product> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load product");
                return;
            }

            Product product = result.getData();
            initializeFormFields(product);
            fallbackManager.setLoading(false);
        });
        return result;
    }

    private Result<Product> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load product."));
        return new Result<>();
    }

    private void initializeFormFields(Product product) {
        nameFormField.initialize(String::new, "Name", true, product.getName(), "Your input is not valid.");
        descriptionFormField.initialize(String::new,"Description", false, product.getDescription(), "Your input is not valid.");
        unitOfMeasurementSelect.initialize(product.getNewUnit().getStandardUnit(), product.getNewUnit().getUnitMagnitude());
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        UpdateProductDTO productDTO = getUpdateProductDTO(organizationId);
        if (productDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        productWriteService.updateProduct(productDTO)
                .thenApply(this::handleUpdateProductResponse)
                .exceptionally(this::handleUpdateProductException);
    }

    private UpdateProductDTO getUpdateProductDTO(Integer organizationId) {
        UpdateProductDTO productDTO = new UpdateProductDTO();
        productDTO.setId(currentSelectionService.getSelectedId());
        productDTO.setOrganizationId(organizationId);
        try {
            productDTO.setName(nameFormField.handleSubmit());
            productDTO.setDescription(descriptionFormField.handleSubmit());
            NewUnitOfMeasurement newUnit = new NewUnitOfMeasurement(unitOfMeasurementSelect.getSelectedUnit(), unitOfMeasurementSelect.getSelectedMagnitude());
            productDTO.setNewUnit(newUnit);
        } catch (ValidationException e) {
            return null;
        }

        return productDTO;
    }

    private Result<Product> handleUpdateProductResponse(Result<Product> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update product.", OperationOutcome.ERROR));
                return;
            }
            Product product = result.getData();
            fallbackManager.setLoading(false);
            toastManager.addToast(new ToastInfo
                    ("Product updated.", "Product has been successfully updated.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(product.getId());
            navigationService.switchView("Product?id=" + product.getId(), true, null);
        });
        return result;
    }

    private Result<Product> handleUpdateProductException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update product.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}
