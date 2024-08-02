package org.chainoptim.desktop.features.product.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.features.product.service.ProductWriteService;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    // Services
    private final ProductService productService;
    private final ProductWriteService productWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final FallbackManager fallbackManager;
    private final ToastManager toastManager;
    private Product product;

    // FXML
    @FXML
    private Label productName;
    @FXML
    private Label productDescription;
    @FXML
    private Button deleteButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab productionTab;
    @FXML
    private Tab pricingTab;
    @FXML
    private Tab evaluationTab;
    @FXML
    private StackPane fallbackContainer;

    @Inject
    public ProductController(ProductService productService,
                             ProductWriteService productWriteService,
                             CommonViewsLoader commonViewsLoader,
                             NavigationService navigationService,
                             CurrentSelectionService currentSelectionService,
                             FallbackManager fallbackManager,
                             ToastManager toastManager) {
        this.productService = productService;
        this.productWriteService = productWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.toastManager = toastManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();
        loadImages();

        Integer productId = currentSelectionService.getSelectedId();
        if (productId != null) {
            loadProduct(productId);
        } else {
            fallbackManager.setErrorMessage("Failed to load product: missing product ID.");
        }
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/product/ProductOverviewView.fxml", this.product);
            }
        });
        productionTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && productionTab.getContent() == null) {
                commonViewsLoader.loadTabContent(productionTab, "/org/chainoptim/desktop/features/product/ProductProductionView.fxml", this.product);
            }
        });
        pricingTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && pricingTab.getContent() == null) {
                commonViewsLoader.loadTabContent(pricingTab, "/org/chainoptim/desktop/features/product/ProductPricingView.fxml", this.product);
            }
        });
        evaluationTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && evaluationTab.getContent() == null) {
                commonViewsLoader.loadTabContent(evaluationTab, "/org/chainoptim/desktop/features/product/ProductEvaluationView.fxml", this.product);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadImages() {
        Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(14);
        deleteImageView.setFitHeight(14);
        deleteButton.setGraphic(deleteImageView);
        deleteButton.setTooltip(new Tooltip("Delete product"));
    }

    private void loadProduct(Integer productId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        productService.getProductWithStages(productId)
                .thenApply(this::handleProductResponse)
                .exceptionally(this::handleProductException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Product> handleProductResponse(Result<Product> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load product.");
                return;
            }
            this.product = result.getData();
            productName.setText(product.getName());
            productDescription.setText(product.getDescription());

            // Load overview tab
            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/product/ProductOverviewView.fxml", this.product);
        });

        return result;
    }

    private Result<Product> handleProductException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load product."));
        return new Result<>();
    }

    @FXML
    private void handleEditProduct() {
        currentSelectionService.setSelectedId(product.getId());
        navigationService.switchView("Update-Product?id=" + product.getId(), true, null);
    }

    @FXML
    private void handleDeleteProduct() {
        fallbackManager.setLoading(true);

        productWriteService.deleteProduct(product.getId())
                .thenApply(this::handleDeleteResponse)
                .exceptionally(this::handleDeleteException);
    }

    private Result<Integer> handleDeleteResponse(Result<Integer> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Failed to delete product.",
                        "An error occurred while deleting the product.",
                        OperationOutcome.ERROR)
                );
                return;
            }

            toastManager.addToast(new ToastInfo(
                    "Product deleted.",
                    "The Product \"" + product.getName() + "\" has been successfully deleted",
                    OperationOutcome.SUCCESS)
            );

            NavigationServiceImpl.invalidateViewCache("Products");
            navigationService.switchView("Products", true, null);
        });
        return result;
    }

    private Result<Integer> handleDeleteException(Throwable ex) {
        Platform.runLater(() ->
            toastManager.addToast(new ToastInfo(
                    "Failed to delete product.",
                    "An error occurred while deleting the product.",
                    OperationOutcome.ERROR)
            )
        );
        return new Result<>();
    }

}
