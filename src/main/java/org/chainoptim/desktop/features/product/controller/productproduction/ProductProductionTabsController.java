package org.chainoptim.desktop.features.product.controller.productproduction;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.features.product.model.TabsActionListener;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.features.scanalysis.productgraph.model.ProductProductionGraph;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Map;

public class ProductProductionTabsController implements TabsActionListener {

    private final FXMLLoaderService fxmlLoaderService;

    private WebView webView;
    private ProductGraphController productGraphController;

    private Product product;

    @FXML
    private TabPane productionTabPane;

    private static final Map<String, String> tabsViewPaths = Map.of(
            "Product Graph", "/org/chainoptim/desktop/features/product/productproduction/ProductGraphView.fxml",
            "Add Stage", "/org/chainoptim/desktop/features/product/productproduction/CreateProductStageView.fxml",
            "Update Stage", "/org/chainoptim/desktop/features/product/productproduction/UpdateProductStageView.fxml"
    );

    @Inject
    public ProductProductionTabsController(FXMLLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    public void initialize(WebView webView, Product product) {
        this.webView = webView;
        this.product = product;

        addTab("Product Graph", null);
    }

    public <T> void addTab(String tabPaneKey, T extraData) {
        Tab tab = new Tab(tabPaneKey);
        FXMLLoader loader = fxmlLoaderService.setUpLoader(tabsViewPaths.get(tabPaneKey), MainApplication.injector::getInstance);
        try {
            Node tabsView = loader.load();

            handleSpecialTabs(tabPaneKey, extraData, loader);

            // Add new tab and select it
            tab.setContent(tabsView);
            tab.getStyleClass().add("custom-tab");
            productionTabPane.getTabs().add(tab);
            productionTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> void handleSpecialTabs(String tabPaneKey, T extraData, FXMLLoader loader) {
        // Inject the webView in the controller in case of Product Graph
        if (tabPaneKey.equals("Product Graph")) {
            productGraphController = loader.getController();
            productGraphController.initialize(webView);
        }
        // Set up Add Stage listener in case of Add Stage
        if (tabPaneKey.equals("Add Stage")) {
            CreateProductStageController controller = loader.getController();
            controller.setActionListener(this);
        }
        // Set up Update Stage listener and send stageId and productId in case of Update Stage
        if (tabPaneKey.equals("Update Stage")) {
            System.out.println("Initializing Update Stage tab with product stage id: " + extraData);
            UpdateProductStageController controller = loader.getController();
            controller.setActionListener(this);
            controller.initialize((Integer) extraData, product.getId());
        }
    }

    private void closeTab(String tabKey) {
        productionTabPane.getTabs().removeIf(tab -> tabKey.equals(tab.getText()));
    }

    private void selectTab(String tabKey) {
        Tab selectedTab = productionTabPane.getTabs().stream()
                .filter(tab -> tabKey.equals(tab.getText()))
                .findFirst()
                .orElse(null);

        if (selectedTab != null) {
            productionTabPane.getSelectionModel().select(selectedTab);
        }
    }

    @Override
    public void onAddStage(ProductProductionGraph productionGraph) {
        // Refresh graph, close Add Stage tab and select Product Graph tab
        if (productGraphController == null) {
            System.out.println("Product Graph Controller is null");
            return;
        }

        Platform.runLater(() -> {
            productGraphController.refreshGraph(productionGraph);
            closeTab("Add Stage");
            selectTab("Product Graph");
        });
    }

    @Override
    public void onUpdateStage(ProductProductionGraph productionGraph) {
        if (productGraphController == null) {
            System.out.println("Product Graph Controller is null");
            return;
        }

        Platform.runLater(() -> {
            productGraphController.refreshGraph(productionGraph);
            closeTab("Update Stage");
            selectTab("Product Graph");
        });
    }
}
