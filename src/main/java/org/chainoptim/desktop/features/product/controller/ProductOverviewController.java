package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import org.chainoptim.desktop.shared.util.DataReceiver;

import java.util.List;

public class ProductOverviewController implements DataReceiver<Product> {

    private final ProductService productService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    private final FallbackManager fallbackManager;
    private Product product;
    private ProductOverviewDTO productOverview;

    @FXML
    private TextFlow descriptionTextFlow;
    @FXML
    private Label unitOfMeasurementLabel;
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
                .thenApply(this::handleProductOverviewResponse)
                .exceptionally(this::handleProductOverviewException);
    }

    private Result<ProductOverviewDTO> handleProductOverviewResponse(Result<ProductOverviewDTO> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load product overview");
                return;
            }
            this.productOverview = result.getData();

            renderUI();
        });

        return result;
    }

    private Result<ProductOverviewDTO> handleProductOverviewException(Throwable throwable) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load product overview"));
        return new Result<>();
    }

    private void renderUI() {
        String description = product.getDescription() != null ? product.getDescription() : "None";
        Text text = new Text(description);
        descriptionTextFlow.getChildren().add(text);
        descriptionTextFlow.getStyleClass().setAll("general-label");
        descriptionTextFlow.setStyle("-fx-padding: 2px 0px 0px 0px;"); // Fix horizontal alignment issue

        String unitName = product.getUnit() != null ? product.getUnit().getName() : "None";
        unitOfMeasurementLabel.setText(unitName);
        unitOfMeasurementLabel.getStyleClass().setAll("general-label");

        renderEntitiesVBox();
    }

    private void renderEntitiesVBox() {
        entitiesVBox.setSpacing(20);

        renderEntityFlowPane("Stages", productOverview.getStages(), "Product");
        renderEntityFlowPane("Manufactured In", productOverview.getManufacturedInFactories(), "Factory");
        renderEntityFlowPane("Stored In", productOverview.getStoredInWarehouses(), "Warehouse");
        renderEntityFlowPane("Ordered By", productOverview.getOrderedByClients(), "Client");
    }

    private void renderEntityFlowPane(String labelText, List<SmallEntityDTO> entityDTOs, String entityPageKey) {
        FlowPane entityFlowPane = new FlowPane();
        entityFlowPane.setHgap(8);
        entityFlowPane.setVgap(8);
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
