package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.util.Map;

public class ProductionTabsController {

    private final FXMLLoaderService fxmlLoaderService;

    private WebView webView;

    @FXML
    private TabPane productionTabPane;

    private static final Map<String, String> tabsViewPaths = Map.of(
            "Factory Graph", "/org/chainoptim/desktop/features/factory/factoryproduction/FactoryGraphView.fxml",
            "Add Stage", "/org/chainoptim/desktop/features/factory/factoryproduction/CreateStageView.fxml"
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
                FactoryGraphController controller = loader.getController();
                controller.initialize(webView);
            }

            tab.setContent(tabsView);
            tab.getStyleClass().add("custom-tab");
            productionTabPane.getTabs().add(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
