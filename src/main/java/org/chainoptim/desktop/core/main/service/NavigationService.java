package org.chainoptim.desktop.core.main.service;

import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Map;

/*
 * Service responsible for handling app navigation in SidebarController
 */
public class NavigationService {

    private static Injector injector;

    @Setter
    private StackPane mainContentArea;

    public static void setInjector(Injector injector) {
        NavigationService.injector = injector;
    }

    @Getter
    private final Map<String, String> viewMap = Map.of(
            "Overview", "/org/chainoptim/desktop/core/main/OverviewView.fxml",
            "Organization", "/org/chainoptim/desktop/core/organization/OrganizationView.fxml",
            "Products", "/org/chainoptim/desktop/features/test/tudor/ProductsView.fxml",
            "Factories", "/org/chainoptim/desktop/features/factory/FactoriesView.fxml",
            "Warehouses", "/org/chainoptim/desktop/features/warehouse/WarehousesView.fxml",
            "Suppliers", "/org/chainoptim/desktop/features/supplier/SuppliersView.fxml"
    );

    public void switchView(String viewKey) {
        String viewPath = viewMap.get(viewKey);
        if (viewPath != null) {
            try {
                System.out.println(viewPath);
                FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
                loader.setControllerFactory(injector::getInstance);
                Node view = loader.load();

                Platform.runLater(() -> {
                    mainContentArea.getChildren().setAll(view);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("View path for " + viewKey + " not found.");
        }
    }
}
