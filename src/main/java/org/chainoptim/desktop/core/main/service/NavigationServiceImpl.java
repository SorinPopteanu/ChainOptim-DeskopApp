package org.chainoptim.desktop.core.main.service;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
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
    private final FallbackManager fallbackManager;

    @Inject
    public NavigationServiceImpl(FXMLLoaderService fxmlLoaderService,
                                 ControllerFactory controllerFactory,
                                 ThreadRunner threadRunner,
                                 FallbackManager fallbackManager) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.threadRunner = threadRunner;
        this.fallbackManager = fallbackManager;
    }

    @Setter
    private StackPane mainContentArea;

    private String currentViewKey;
    private List<String> previousViewKeys;

    @Getter
    private static final Map<String, Node> viewCache = new HashMap<>();

    private final Map<String, String> viewMap = Map.ofEntries(
            Map.entry("Overview", "/org/chainoptim/desktop/core/main/OverviewView.fxml"),
            Map.entry("Organization", "/org/chainoptim/desktop/core/organization/OrganizationView.fxml"),

            Map.entry("Products", "/org/chainoptim/desktop/features/product/ProductsView.fxml"),
            Map.entry("Product", "/org/chainoptim/desktop/features/product/ProductView.fxml"),
            Map.entry("Create-Product", "/org/chainoptim/desktop/features/product/CreateProductView.fxml"),

            Map.entry("Factories", "/org/chainoptim/desktop/features/factory/FactoriesView.fxml"),
            Map.entry("Factory", "/org/chainoptim/desktop/features/factory/FactoryView.fxml"),
            Map.entry("Create-Factory", "/org/chainoptim/desktop/features/factory/CreateFactoryView.fxml"),
            Map.entry("Update-Factory", "/org/chainoptim/desktop/features/factory/UpdateFactoryView.fxml"),

            Map.entry("Warehouses", "/org/chainoptim/desktop/features/warehouse/WarehousesView.fxml"),
            Map.entry("Warehouse", "/org/chainoptim/desktop/features/warehouse/WarehouseView.fxml"),
            Map.entry("Create-Warehouse", "/org/chainoptim/desktop/features/warehouse/CreateWarehouseView.fxml"),

            Map.entry("Suppliers", "/org/chainoptim/desktop/features/supplier/SuppliersView.fxml"),
            Map.entry("Supplier", "/org/chainoptim/desktop/features/supplier/SupplierView.fxml"),
            Map.entry("Create-Supplier", "/org/chainoptim/desktop/features/supplier/CreateSupplierView.fxml"),

            Map.entry("Clients", "/org/chainoptim/desktop/features/client/ClientsView.fxml"),
            Map.entry("Client", "/org/chainoptim/desktop/features/client/ClientView.fxml"),
            Map.entry("Create-Client", "/org/chainoptim/desktop/features/client/CreateClientView.fxml"),

            Map.entry("Create-Stage", "/org/chainoptim/desktop/features/client/CreateFactoryStageView.fxml")
    );

    public void switchView(String viewKey, boolean forward) {
        // Skip if already there
        if (Objects.equals(currentViewKey, viewKey)) {
            return;
        }

        // Reset fallback state between pages
        fallbackManager.reset();

        // Get view from cache or load it
        Node view = viewCache.computeIfAbsent(viewKey, this::loadView);

        // Display view
        if (view != null) {
            threadRunner.runLater(() -> mainContentArea.getChildren().setAll(view));
            handleHistory(forward);
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

    private void handleHistory(boolean forward) {
        // Add to history if forward and remove last otherwise
        if (forward) {
            if (previousViewKeys == null) {
                previousViewKeys = new ArrayList<>();
            }
            if (currentViewKey != null) {
                previousViewKeys.add(currentViewKey);
            }
        } else {
            if (previousViewKeys != null && !previousViewKeys.isEmpty()) {
                previousViewKeys.removeLast();
            }
        }
    }

    public void goBack() {
        if (previousViewKeys != null && !previousViewKeys.isEmpty()) {
            switchView(previousViewKeys.getLast(), false);
        }
    }

    public static void invalidateViewCache() {
        viewCache.clear();
    }

    public static void invalidateViewCache(String viewKey) {
        viewCache.remove(viewKey);
    }
}
