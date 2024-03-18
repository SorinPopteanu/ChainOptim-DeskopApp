package org.chainoptim.desktop.core.main.service;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Service responsible for handling app navigation from SidebarController
 * Loads views on demand and caches them, including dynamic routes
 *
 */
public class NavigationServiceImpl implements NavigationService {

    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final ThreadRunner threadRunner;

    @Inject
    public NavigationServiceImpl(FXMLLoaderService fxmlLoaderService, ControllerFactory controllerFactory, ThreadRunner threadRunner) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.threadRunner = threadRunner;
    }

    @Setter
    private StackPane mainContentArea;

    private String currentViewKey;

    @Getter
    private static final Map<String, Node> viewCache = new HashMap<>();

    private final Map<String, String> viewMap = Map.ofEntries(
            // Main pages
            Map.entry("Overview", "/org/chainoptim/desktop/core/main/OverviewView.fxml"),
            Map.entry("Organization", "/org/chainoptim/desktop/core/organization/OrganizationView.fxml"),
            Map.entry("Products", "/org/chainoptim/desktop/features/product/ProductsView.fxml"),
            Map.entry("Factories", "/org/chainoptim/desktop/features/factory/FactoriesView.fxml"),
            Map.entry("Warehouses", "/org/chainoptim/desktop/features/warehouse/WarehousesView.fxml"),
            Map.entry("Suppliers", "/org/chainoptim/desktop/features/supplier/SuppliersView.fxml"),
            Map.entry("Clients", "/org/chainoptim/desktop/features/client/ClientsView.fxml"),
            // Dynamic route pages
            Map.entry("Product", "/org/chainoptim/desktop/features/product/ProductView.fxml"),
            Map.entry("Factory", "/org/chainoptim/desktop/features/factory/FactoryView.fxml"),
            Map.entry("Supplier", "/org/chainoptim/desktop/features/supplier/SupplierView.fxml"),
            Map.entry("Client", "/org/chainoptim/desktop/features/client/ClientView.fxml"),

            // Create forms
            Map.entry("Create-Product", "/org/chainoptim/desktop/features/product/CreateProductView.fxml"),
            Map.entry("Create-Supplier", "/org/chainoptim/desktop/features/supplier/CreateSupplierView.fxml"),
            Map.entry("Create-Client", "/org/chainoptim/desktop/features/client/CreateClientView.fxml")
    );

    public void switchView(String viewKey) {
        // Skip if already there
        if (Objects.equals(currentViewKey, viewKey)) {
            return;
        }
        System.out.println(viewCache);

        // Get view from cache or load it
        Node view = viewCache.computeIfAbsent(viewKey, this::loadView);

        // Display view
        if (view != null) {
            threadRunner.runLater(() -> mainContentArea.getChildren().setAll(view));
            currentViewKey = viewKey;
        }
    }

    private Node loadView(String viewKey) {
        // Extract key without dynamic parameter
        String baseViewKey = findBaseKey(viewKey);

        // Get View path
        String viewPath = viewMap.get(baseViewKey);
        if (viewPath == null) {
            System.out.println("View path for " + baseViewKey + " not found.");
            return null;
        }

        // Load view
        return fxmlLoaderService.loadView(viewPath, controllerFactory::createController);
    }

    private String findBaseKey(String viewKey) {
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
