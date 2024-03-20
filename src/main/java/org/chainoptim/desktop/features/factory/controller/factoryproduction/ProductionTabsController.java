package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.JavaConnector;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.util.Map;

public class ProductionTabsController implements TabsActionListener {

    private final FXMLLoaderService fxmlLoaderService;

    private WebView webView;
    private FactoryGraphController factoryGraphController;

    private Factory factory;

    @FXML
    private TabPane productionTabPane;

    private static final Map<String, String> tabsViewPaths = Map.of(
            "Factory Graph", "/org/chainoptim/desktop/features/factory/factoryproduction/FactoryGraphView.fxml",
            "Add Stage", "/org/chainoptim/desktop/features/factory/factoryproduction/CreateFactoryStageView.fxml",
            "Update Stage", "/org/chainoptim/desktop/features/factory/factoryproduction/UpdateFactoryStageView.fxml"
    );

    @Inject
    public ProductionTabsController(FXMLLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    public void initialize(WebView webView, Factory factory) {
        this.webView = webView;
        this.factory = factory;

        addTab("Factory Graph", null);
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
        // Inject the webView in the controller in case of Factory Graph
        if (tabPaneKey.equals("Factory Graph")) {
            factoryGraphController = loader.getController();
            factoryGraphController.initialize(webView);
        }
        // Set up Add Stage listener in case of Add Stage
        if (tabPaneKey.equals("Add Stage")) {
            CreateFactoryStageController controller = loader.getController();
            controller.setActionListener(this);
        }
        if (tabPaneKey.equals("Update Stage")) {
            System.out.println("Initializing Update Stage tab with factory stage id: " + extraData);
            UpdateFactoryStageController controller = loader.getController();
            controller.setActionListener(this);
            controller.initialize((Integer) extraData, factory.getId());
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
    public void onAddStage(FactoryProductionGraph productionGraph) {
        // Refresh graph, close Add Stage tab and select Factory Graph tab
        if (factoryGraphController != null) {
            Platform.runLater(() -> {
                factoryGraphController.refreshGraph(productionGraph);
                closeTab("Add Stage");
                selectTab("Factory Graph");
            });
        } else {
            System.out.println("Factory Graph Controller is null");
        }
    }

    @Override
    public void onUpdateStage(FactoryProductionGraph productionGraph) {
        if (factoryGraphController != null) {
            Platform.runLater(() -> {
                factoryGraphController.refreshGraph(productionGraph);
                closeTab("Update Stage");
                selectTab("Factory Graph");
            });
        } else {
            System.out.println("Factory Graph Controller is null");
        }
    }
}
