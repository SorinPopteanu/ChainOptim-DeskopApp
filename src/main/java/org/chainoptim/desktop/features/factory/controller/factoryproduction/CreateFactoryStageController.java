package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.factory.service.FactoryStageWriteService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.shared.common.uielements.SelectDurationController;
import org.chainoptim.desktop.shared.common.uielements.SelectFactoryController;
import org.chainoptim.desktop.shared.common.uielements.SelectStageController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import lombok.Setter;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class CreateFactoryStageController implements Initializable {

    private final FactoryStageWriteService factoryStageWriteService;
    private final FactoryProductionGraphService graphService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @Setter
    private TabsActionListener actionListener;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private StackPane selectStageContainer;
    private SelectStageController selectStageController;
    @FXML
    private StackPane selectFactoryContainer;
    private SelectFactoryController selectFactoryController;
    @FXML
    private TextField capacityField;
    @FXML
    private StackPane durationInputContainer;
    private SelectDurationController selectDurationController;
    @FXML
    private TextField priorityField;
    @FXML
    private TextField minimumRequiredCapacityField;

    @Inject
    public CreateFactoryStageController(
            FactoryStageWriteService factoryStageWriteService,
            FactoryProductionGraphService graphService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.factoryStageWriteService = factoryStageWriteService;
        this.graphService = graphService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectStageView();
        loadSelectFactoryView();
        loadSelectDurationView();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadSelectStageView() {
        // Initialize time selection input view
        FXMLLoader selectStageLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectStageView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectStageView = selectStageLoader.load();
            selectStageController = selectStageLoader.getController();
            selectStageController.initialize();
            selectStageContainer.getChildren().add(selectStageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectFactoryView() {
        // Initialize time selection input view
        FXMLLoader selectFactoryLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectFactoryView.fxml",
                MainApplication.injector::getInstance
        );
        try {
            Node selectFactoryView = selectFactoryLoader.load();
            selectFactoryController = selectFactoryLoader.getController();
            selectFactoryController.initialize();
            selectFactoryContainer.getChildren().add(selectFactoryView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectDurationView() {
        // Initialize time selection input view
        FXMLLoader timeInputLoader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectDurationView.fxml",
                MainApplication.injector::getInstance
        );
        try {
            Node timeInputView = timeInputLoader.load();
            selectDurationController = timeInputLoader.getController();
            durationInputContainer.getChildren().add(timeInputView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        CreateFactoryStageDTO stageDTO = getStageDTO();

        System.out.println(stageDTO);

        factoryStageWriteService.createFactoryStage(stageDTO, true)
                .thenAccept(stageOptional ->
                    Platform.runLater(() -> {
                        if (stageOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create stage.");
                            return;
                        }
                        FactoryStage stage = stageOptional.get();
                        fallbackManager.setLoading(false);

                        graphService.refreshFactoryGraph(stageDTO.getFactoryId()).thenApply(productionGraphOptional -> {
                            Platform.runLater(() -> {
                                if (productionGraphOptional.isEmpty()) {
                                    fallbackManager.setErrorMessage("Failed to refresh factory graph");
                                }
                                if (actionListener != null && productionGraphOptional.isPresent()) {
                                    actionListener.onAddStage(productionGraphOptional.get());
                                }
                            });
                            return productionGraphOptional;
                        });
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateFactoryStageDTO getStageDTO() {
        CreateFactoryStageDTO stageDTO = new CreateFactoryStageDTO();

        Integer stageId = selectStageController.getSelectedStage().getId();
        stageDTO.setStageId(stageId);
        Integer factoryId = selectFactoryController.getSelectedFactory().getId();
        stageDTO.setFactoryId(factoryId);

        stageDTO.setCapacity(parseFloat(capacityField.getText()));
        stageDTO.setDuration(selectDurationController.getTimeSeconds());
        stageDTO.setPriority(parseInt(priorityField.getText()));
        stageDTO.setMinimumRequiredCapacity(parseFloat(minimumRequiredCapacityField.getText()));

        return stageDTO;
    }

}
