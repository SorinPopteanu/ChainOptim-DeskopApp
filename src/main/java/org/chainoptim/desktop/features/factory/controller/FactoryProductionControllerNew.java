package org.chainoptim.desktop.features.factory.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.features.factory.controller.factoryproduction.ProductionTabsController;
import org.chainoptim.desktop.features.factory.controller.factoryproduction.ProductionToolbarController;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.util.Objects;

public class FactoryProductionControllerNew implements DataReceiver<Factory> {

    private final FXMLLoaderService fxmlLoaderService;

    private Factory factory;


    private WebView webView;

    @FXML
    private StackPane tabsContainer;

    @FXML
    private StackPane toolbarContainer;

    @Inject
    public FactoryProductionControllerNew(FXMLLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
        initializeWebView();
        loadTabs();
        loadToolbar();
    }

    private void initializeWebView() {
        webView = new WebView();
        webView.getEngine().load(Objects.requireNonNull(getClass().getResource("/html/graph.html")).toExternalForm());
    }

    private void loadTabs() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/features/factory/factoryproduction/ProductionTabsView.fxml", MainApplication.injector::getInstance);
        try {
            Node tabsView = loader.load();
            tabsContainer.getChildren().add(tabsView);
            ProductionTabsController tabsController = loader.getController();
            tabsController.initialize(webView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadToolbar() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/features/factory/factoryproduction/ProductionToolbarView.fxml", MainApplication.injector::getInstance);
        try {
            Node toolbarView = loader.load();
            toolbarContainer.getChildren().add(toolbarView);
            ProductionToolbarController toolbarController = loader.getController();
            toolbarController.initialize(webView, factory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
