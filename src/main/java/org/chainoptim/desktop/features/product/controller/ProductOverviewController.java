package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.overview.model.Snapshot;
import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.shared.common.uielements.badge.BadgeData;
import org.chainoptim.desktop.shared.common.uielements.badge.FeatureCountBadge;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import org.chainoptim.desktop.shared.util.DataReceiver;

import java.util.List;
import java.util.Optional;

public class ProductOverviewController implements DataReceiver<Product> {

    private final ProductService productService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;
    private Product product;
    private ProductOverviewDTO productOverview;


    @FXML
    private VBox entitiesVBox;

    @Inject
    public ProductOverviewController(ProductService productService,
                                     NavigationService navigationService,
                                     CurrentSelectionService currentSelectionService,
                                     FallbackManager fallbackManager) {
        this.productService = productService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Product product) {
        this.product = product;

        loadProductOverview();
    }

    private void loadProductOverview() {
        fallbackManager.reset();
        fallbackManager.setLoading(false);

        productService.getProductOverview(product.getId())
                .thenApply(this::handleProductOverviewResponse);
    }

    private Optional<ProductOverviewDTO> handleProductOverviewResponse(Optional<ProductOverviewDTO> overviewDTO) {
        Platform.runLater(() -> {
            if (overviewDTO.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load product overview");
                return;
            }
            this.productOverview = overviewDTO.get();
            System.out.println("Product Overview: " + productOverview);

            renderEntitiesVBox();
        });

        return overviewDTO;
    }

    private void renderEntitiesVBox() {
        entitiesVBox.setSpacing(20);

        renderEntityHBox("Stages", productOverview.getStages(), "Product");
        renderEntityHBox("Manufactured In", productOverview.getManufacturedInFactories(), "Factory");
        renderEntityHBox("Stored In", productOverview.getStoredInWarehouses(), "Warehouse");
        renderEntityHBox("Ordered By", productOverview.getOrderedByClients(), "Client");
    }

    private void renderEntityHBox(String labelText, List<SmallEntityDTO> entityDTOs, String entityPageKey) {
        FlowPane entityFlowPane = new FlowPane();
        entityFlowPane.setHgap(8);
        entityFlowPane.setVgap(4);
        entityFlowPane.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText + ":");
        label.getStyleClass().setAll("general-label-medium-large");
        entityFlowPane.getChildren().add(label);

        if (entityDTOs.isEmpty()) {
            Label noEntitiesLabel = new Label("None");
            noEntitiesLabel.getStyleClass().setAll("general-label");
            entityFlowPane.getChildren().add(noEntitiesLabel);
            entitiesVBox.getChildren().add(entityFlowPane);
            return;
        }

        for (int i = 0; i < entityDTOs.size(); i++) {
            SmallEntityDTO entityDTO = entityDTOs.get(i);
            String nameText = i != entityDTOs.size() - 1 ? entityDTO.getName() + ", " : entityDTO.getName();
            Label entityLabel = new Label(nameText);
            entityLabel.getStyleClass().setAll("pseudo-link", "general-label");

            entityLabel.setOnMouseClicked(event -> {
                currentSelectionService.setSelectedId(entityDTO.getId());
                navigationService.switchView(entityPageKey + "?id=" + entityDTO.getId(), true);
            });

            entityFlowPane.getChildren().add(entityLabel);
        }

        entitiesVBox.getChildren().add(entityFlowPane);
    }
}
