package org.chainoptim.desktop.core.main.service;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.chainoptim.desktop.MainApplication;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Service responsible for handling app navigation in SidebarController
 * Loads views on demand and caches them, including dynamic routes
 *
 */
public class NavigationService {

    @Setter
    private StackPane mainContentArea;

    private String currentViewKey;

    private static final Map<String, Node> viewCache = new HashMap<>();

    @Getter
    private final Map<String, String> viewMap = Map.of(
            // Main pages
            "Overview", "/org/chainoptim/desktop/core/main/OverviewView.fxml",
            "Organization", "/org/chainoptim/desktop/core/organization/OrganizationView.fxml",
            "Products", "/org/chainoptim/desktop/features/test/tudor/ProductsView.fxml",
            "Factories", "/org/chainoptim/desktop/features/factory/FactoriesView.fxml",
            "Warehouses", "/org/chainoptim/desktop/features/warehouse/WarehousesView.fxml",
            "Suppliers", "/org/chainoptim/desktop/features/supplier/SuppliersView.fxml",
            // Dynamic route pages
            "Product", "/org/chainoptim/desktop/features/test/tudor/ProductView.fxml"
    );

    private final Map<String, String> cssMap = Map.of(
            "Overview", "/css/overview.css",
            "Organization", "/css/organization.css",
            "Products", "/css/products.css",
            "Factories", "/css/factories.css",
            "Warehouses", "/css/warehouses.css",
            "Suppliers", "/css/suppliers.css"
    );

    public void switchView(String viewKey) {
        // Skip if already there
        // viewKey = Product?id=1
        if (Objects.equals(currentViewKey, viewKey)) {
            return;
        }

        // Get view from cache or load it
        Node view = viewCache.computeIfAbsent(viewKey, this::loadView);

        // Display view
        if (view != null) {
            Platform.runLater(() -> mainContentArea.getChildren().setAll(view));
            currentViewKey = viewKey;
        }
    }

    private Node loadView(String viewKey) {
        // viewKey = Product
        // Extract key without dynamic parameter
        String baseViewKey = findBaseKey(viewKey);

        String viewPath = viewMap.get(baseViewKey);
        if (viewPath == null) {
            System.out.println("View path for " + baseViewKey + " not found.");
            return null;
        }

        try {
            System.out.println("Loading view: " + viewPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            loader.setControllerFactory(MainApplication.injector::getInstance);
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String findBaseKey(String viewKey) {

        // viewKey = Product?id=1
        Pattern pattern = Pattern.compile("([^?]+)\\?id=(\\d+)");
        Matcher matcher = pattern.matcher(viewKey);

        String baseViewKey;
        Integer id = null;

        if (matcher.find()) {
            baseViewKey = matcher.group(1);
            id = Integer.valueOf(matcher.group(2));
        } else {
            baseViewKey = viewKey;
        }
        System.out.println("Requested id: " + id);

        return baseViewKey;
    }

    public static void invalidateViewCache() {
        viewCache.clear();
    }
}
