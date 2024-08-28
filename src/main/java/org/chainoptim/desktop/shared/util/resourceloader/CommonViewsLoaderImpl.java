package org.chainoptim.desktop.shared.util.resourceloader;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.map.MapController;
import org.chainoptim.desktop.core.overview.controller.TimelineController;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.shared.common.uielements.select.*;
import org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.table.TableToolbarController;
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
                "/org/chainoptim/desktop/shared/search/ListHeaderView.fxml",
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

    public TableToolbarController initializeTableToolbar(StackPane tableToolbarContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/table/TableToolbarView.fxml",
                controllerFactory::createController
        );
        try {
            Node tableToolbarView = loader.load();
            tableToolbarContainer.getChildren().add(tableToolbarView);
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

    public <T> GenericConfirmDialogController<T> loadConfirmDialog(StackPane confirmDialogContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/confirmdialog/GenericConfirmDialogView.fxml",
                controllerFactory::createController);

        try {
            Node confirmDialogView = loader.load();
            confirmDialogContainer.getChildren().add(confirmDialogView);
            return loader.getController();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public SelectOrCreateLocationController loadSelectOrCreateLocation(StackPane selectOrCreateLocationContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/select/SelectOrCreateLocationView.fxml",
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

    public SelectDurationController loadSelectDurationView(StackPane durationInputContainer) {
        FXMLLoader timeInputLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/select/SelectDurationView.fxml",
                controllerFactory::createController
        );
        try {
            Node timeInputView = timeInputLoader.load();
            durationInputContainer.getChildren().add(timeInputView);
            return timeInputLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SelectStageController loadSelectStageView(StackPane selectStageContainer) {
        // Initialize time selection input view
        FXMLLoader selectStageLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/select/SelectStageView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectStageView = selectStageLoader.load();
            selectStageContainer.getChildren().add(selectStageView);
            return selectStageLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SelectFactoryController loadSelectFactoryView(StackPane selectFactoryContainer) {
        // Initialize time selection input view
        FXMLLoader selectFactoryLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/select/SelectFactoryView.fxml",
                MainApplication.injector::getInstance
        );
        try {
            Node selectFactoryView = selectFactoryLoader.load();
            selectFactoryContainer.getChildren().add(selectFactoryView);
            return selectFactoryLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MapController loadSupplyChainMap(StackPane mapContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/map/MapView.fxml",
                controllerFactory::createController
        );
        try {
            Node mapView = loader.load();
            mapContainer.getChildren().add(mapView);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public TimelineController loadTimeline(StackPane timelineContainer) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/overview/TimelineView.fxml",
                controllerFactory::createController
        );
        try {
            Node timelineView = loader.load();
            timelineContainer.getChildren().add(timelineView);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
