package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.overview.model.Snapshot;
import org.chainoptim.desktop.features.product.dto.ProductOverviewDTO;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.product.service.ProductService;
import org.chainoptim.desktop.shared.common.uielements.badge.BadgeData;
import org.chainoptim.desktop.shared.common.uielements.badge.FeatureCountBadge;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ProductOverviewController implements DataReceiver<Product> {

    private final ProductService productService;
    private final NavigationService navigationService;

    private final FallbackManager fallbackManager;
    private Product product;
    private ProductOverviewDTO productOverview;


    @FXML
    private VBox entitiesVBox;

    @Inject
    public ProductOverviewController(ProductService productService,
                                     NavigationService navigationService,
                                     FallbackManager fallbackManager) {
        this.productService = productService;
        this.navigationService = navigationService;
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
                .thenAccept(productOverviewDTO -> {
                    if (productOverviewDTO == null) {
                        fallbackManager.setErrorMessage("Failed to load product overview");
                        return;
                    }
                    this.productOverview = productOverviewDTO;
                    System.out.println("Product Overview: " + productOverview);

                    renderEntitiesVBox();
                });
    }

    private void renderEntitiesVBox() {
//        entityCountsHBox.getChildren().clear();

//        FeatureCountBadge productsCountLabel = new FeatureCountBadge(new BadgeData("Stages", 4, () -> navigationService.switchView("Products", true)));
//        FeatureCountBadge factoriesCountLabel = new FeatureCountBadge(new BadgeData("Needed Components", 5, () -> navigationService.switchView("Factories", true)));
//        FeatureCountBadge warehousesCountLabel = new FeatureCountBadge(new BadgeData("Manufactured In", 3, () -> navigationService.switchView("Warehouses", true)));
//        FeatureCountBadge suppliersCountLabel = new FeatureCountBadge(new BadgeData("Stored In", 2, () -> navigationService.switchView("Suppliers", true)));
//        FeatureCountBadge clientsCountLabel = new FeatureCountBadge(new BadgeData("Ordered By", 6, () -> navigationService.switchView("Clients", true)));

//        entityCountsHBox.getChildren().addAll(productsCountLabel, factoriesCountLabel, warehousesCountLabel, suppliersCountLabel, clientsCountLabel);
//        entityCountsHBox.setSpacing(40);
//        entityCountsHBox.setAlignment(Pos.CENTER);
    }

    private void renderEntityHBox() {

    }
}
