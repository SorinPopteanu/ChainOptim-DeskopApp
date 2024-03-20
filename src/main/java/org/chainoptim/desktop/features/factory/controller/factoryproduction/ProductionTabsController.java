package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import lombok.Setter;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.features.factory.model.ProductionToolbarActionListener;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryGraph;
import org.chainoptim.desktop.features.scanalysis.factorygraph.model.FactoryProductionGraph;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.util.Map;

import static org.chainoptim.desktop.shared.util.JsonUtil.prepareJsonString;

public class ProductionTabsController implements TabsActionListener {

    private final FXMLLoaderService fxmlLoaderService;

    private WebView webView;
    private FactoryGraphController factoryGraphController;

    @FXML
    private TabPane productionTabPane;

    private static final Map<String, String> tabsViewPaths = Map.of(
            "Factory Graph", "/org/chainoptim/desktop/features/factory/factoryproduction/FactoryGraphView.fxml",
            "Add Stage", "/org/chainoptim/desktop/features/factory/factoryproduction/CreateFactoryStageView.fxml"
    );

    @Inject
    public ProductionTabsController(FXMLLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    public void initialize(WebView webView) {
        this.webView = webView;

        addTab("Factory Graph");
    }

    public void addTab(String tabPaneKey) {
        Tab tab = new Tab(tabPaneKey);
        FXMLLoader loader = fxmlLoaderService.setUpLoader(tabsViewPaths.get(tabPaneKey), MainApplication.injector::getInstance);
        try {
            Node tabsView = loader.load();

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

            tab.setContent(tabsView);
            tab.getStyleClass().add("custom-tab");
            productionTabPane.getTabs().add(tab);

            // Select the newly added tab
            productionTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
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
}
