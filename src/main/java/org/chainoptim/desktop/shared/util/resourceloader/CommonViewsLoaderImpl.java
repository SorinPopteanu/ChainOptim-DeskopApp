package org.chainoptim.desktop.shared.util.resourceloader;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateUnitOfMeasurementController;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class CommonViewsLoaderImpl implements CommonViewsLoader {

    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    @Inject
    public CommonViewsLoaderImpl(FXMLLoaderService fxmlLoaderService, ControllerFactory controllerFactory) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

    public void loadFallbackManager(StackPane fallbackContainer) {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    public ListHeaderController loadListHeader(StackPane headerContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/main/ListHeaderView.fxml",
                controllerFactory::createController
        );
        try {
            Node headerView = loader.load();
            headerContainer.getChildren().add(headerView);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PageSelectorController loadPageSelector(StackPane pageSelectorContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/search/PageSelectorView.fxml",
                controllerFactory::createController
        );
        try {
            Node pageSelectorView = loader.load();
            pageSelectorContainer.getChildren().add(pageSelectorView);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> void loadTabContent(Tab tab, String fxmlFilepath, T data) {
        try {
            FXMLLoader loader = fxmlLoaderService.setUpLoader(fxmlFilepath, controllerFactory::createController);
            Node content = loader.load();
            DataReceiver<T> controller = loader.getController();
            controller.setData(data);
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SelectOrCreateLocationController loadSelectOrCreateLocation(StackPane selectOrCreateLocationContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateLocationView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateLocationView = loader.load();
            selectOrCreateLocationContainer.getChildren().add(selectOrCreateLocationView);
            return loader.getController();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public SelectOrCreateUnitOfMeasurementController loadSelectOrCreateUnitOfMeasurement(StackPane unitOfMeasurementContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateUnitOfMeasurementView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateUnitOfMeasurementView = loader.load();
            unitOfMeasurementContainer.getChildren().add(selectOrCreateUnitOfMeasurementView);
            return loader.getController();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
