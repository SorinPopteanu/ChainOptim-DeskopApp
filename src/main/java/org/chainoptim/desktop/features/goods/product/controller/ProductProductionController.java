package org.chainoptim.desktop.features.goods.product.controller;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.features.goods.product.model.ProductionToolbarActionListener;
import org.chainoptim.desktop.features.goods.product.controller.productproduction.ProductProductionTabsController;
import org.chainoptim.desktop.features.goods.product.controller.productproduction.ProductProductionToolbarController;
import org.chainoptim.desktop.features.goods.product.model.Product;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.JavaConnector;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.io.IOException;
import java.util.Objects;

public class ProductProductionController implements DataReceiver<Product>, ProductionToolbarActionListener {

    private final FXMLLoaderService fxmlLoaderService;

    private Product product;

    private WebView webView;
    private JavaConnector javaConnector;

    private ProductProductionTabsController productionTabsController;

    @FXML
    private StackPane tabsContainer;
    @FXML
    private StackPane toolbarContainer;

    @Inject
    public ProductProductionController(FXMLLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    @Override
    public void setData(Product product) {
        this.product = product;
        initializeWebView();
        loadTabs();
        loadToolbar();
    }

    private void initializeWebView() {
        webView = new WebView();
        webView.getEngine().load(Objects.requireNonNull(getClass().getResource("/html/productgraph.html")).toExternalForm());
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webView.getEngine().executeScript("window");
                javaConnector = new JavaConnector();
                jsObject.setMember("javaConnector", javaConnector);
            }
        });
    }

    private void loadTabs() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/features/goods/productproduction/ProductProductionTabsView.fxml", MainApplication.injector::getInstance);
        try {
            Node tabsView = loader.load();
            tabsContainer.getChildren().add(tabsView);
            productionTabsController = loader.getController();
            productionTabsController.initialize(webView, product);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadToolbar() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/features/goods/productproduction/ProductProductionToolbarView.fxml", MainApplication.injector::getInstance);
        try {
            Node toolbarView = loader.load();
            toolbarContainer.getChildren().add(toolbarView);
            ProductProductionToolbarController toolbarController = loader.getController();
            toolbarController.initialize(webView, product);
            toolbarController.setActionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpenAddStageRequested() {
        productionTabsController.addTab("Add Stage", null);
    }

    @Override
    public void onOpenUpdateStageRequested() {
        System.out.println("Parent listening");
        Integer productStageId = javaConnector.getSelectedNodeId();
        if (productStageId != null) {
            System.out.println("Selected product stage: " + productStageId);
            productionTabsController.addTab("Update Stage", productStageId);
        }
    }
}
