package org.chainoptim.desktop.features.product.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.overview.model.Snapshot;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.common.uielements.badge.BadgeData;
import org.chainoptim.desktop.shared.common.uielements.badge.FeatureCountBadge;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class ProductOverviewController implements DataReceiver<Product> {

    private final NavigationService navigationService;
    private Product product;

    @FXML
    private HBox entityCountsHBox;

    @Inject
    public ProductOverviewController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @Override
    public void setData(Product product) {
        this.product = product;

        renderEntityCountsVBox();
    }

    private void renderEntityCountsVBox() {
        entityCountsHBox.getChildren().clear();

        FeatureCountBadge productsCountLabel = new FeatureCountBadge(new BadgeData("Stages", 4, () -> navigationService.switchView("Products", true)));
        FeatureCountBadge factoriesCountLabel = new FeatureCountBadge(new BadgeData("Needed Components", 5, () -> navigationService.switchView("Factories", true)));
        FeatureCountBadge warehousesCountLabel = new FeatureCountBadge(new BadgeData("Manufactured In", 3, () -> navigationService.switchView("Warehouses", true)));
        FeatureCountBadge suppliersCountLabel = new FeatureCountBadge(new BadgeData("Stored In", 2, () -> navigationService.switchView("Suppliers", true)));
        FeatureCountBadge clientsCountLabel = new FeatureCountBadge(new BadgeData("Ordered By", 6, () -> navigationService.switchView("Clients", true)));

        entityCountsHBox.getChildren().addAll(productsCountLabel, factoriesCountLabel, warehousesCountLabel, suppliersCountLabel, clientsCountLabel);
        entityCountsHBox.setSpacing(40);
        entityCountsHBox.setAlignment(Pos.CENTER);
    }
}
