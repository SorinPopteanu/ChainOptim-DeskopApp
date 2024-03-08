package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.dto.CreateProductDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductWriteService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreateProductController implements Initializable {

    private final ProductWriteService productWriteService;
    private final NavigationService navigationService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @FXML
    private Label nameLabel;

    @FXML
    private TextField nameField;

    @FXML
    private Label descriptionLabel;

    @FXML
    private TextField descriptionField;

    @FXML
    private StackPane fallbackContainer;

    @Inject
    public CreateProductController(
            ProductWriteService productWriteService,
            NavigationService navigationService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.productWriteService = productWriteService;
        this.navigationService = navigationService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
    }

    private void loadFallbackManager() {
        // Load view into fallbackContainer
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    @FXML
    private void handleSubmit() {
        // Get current organization
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        fallbackManager.setLoading(true);

        Integer organizationId = currentUser.getOrganization().getId();

        CreateProductDTO productDTO = new CreateProductDTO();
        productDTO.setName("test name");
        productDTO.setDescription("test");
        productDTO.setUnitId(1);
        productDTO.setOrganizationId(organizationId);

        System.out.println(productDTO);
        productWriteService.createProduct(productDTO)
                .thenAccept(productOptional -> {
                    // Navigate to product page
                    Platform.runLater(() -> {
                        if (productOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create product.");
                            fallbackManager.setLoading(false);
                            return;
                        }
                        navigationService.switchView("Product?=" + productOptional.get().getId());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
