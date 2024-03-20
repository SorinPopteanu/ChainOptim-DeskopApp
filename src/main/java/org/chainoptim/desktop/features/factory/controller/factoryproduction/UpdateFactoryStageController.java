package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.factory.service.FactoryStageService;
import org.chainoptim.desktop.features.factory.service.FactoryStageWriteService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.shared.common.uielements.SelectDurationController;
import org.chainoptim.desktop.shared.common.uielements.SelectFactoryController;
import org.chainoptim.desktop.shared.common.uielements.SelectStageController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class UpdateFactoryStageController {

    private final FactoryStageService factoryStageService;
    private final FactoryStageWriteService factoryStageWriteService;
    private final FactoryProductionGraphService graphService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private FactoryStage factoryStage;
    private Integer factoryId;

    @Setter
    private TabsActionListener actionListener;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private Label stageNameLabel;
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
    public UpdateFactoryStageController(
            FactoryStageService factoryStageService,
            FactoryStageWriteService factoryStageWriteService,
            FactoryProductionGraphService graphService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.factoryStageService = factoryStageService;
        this.factoryStageWriteService = factoryStageWriteService;
        this.graphService = graphService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(Integer factoryStageId, Integer factoryId) {
        this.factoryId = factoryId;
        loadFallbackManager();
        loadFactoryStageIntoForm(factoryStageId);
        loadSelectDurationView();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadFactoryStageIntoForm(Integer factoryStageId) {
        factoryStageService.getFactoryStageById(factoryStageId)
                .thenAccept(factoryStageOptional -> {
                    if (factoryStageOptional.isEmpty()) {
                        fallbackManager.setErrorMessage("Failed to load factory stage");
                        return;
                    }
                    FactoryStage factoryStage = factoryStageOptional.get();
                    System.out.println("Factory stage: " + factoryStage);
                    Platform.runLater(() -> {
                        this.factoryStage = factoryStage;

                        stageNameLabel.setText("Stage: " + factoryStage.getStage().getName());
                        capacityField.setText(String.valueOf(factoryStage.getCapacity()));
                        selectDurationController.setTime(factoryStage.getDuration());
                        priorityField.setText(String.valueOf(factoryStage.getPriority()));
                        minimumRequiredCapacityField.setText(String.valueOf(factoryStage.getMinimumRequiredCapacity()));
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
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

        UpdateFactoryStageDTO stageDTO = getStageDTO();

        System.out.println(stageDTO);

        factoryStageWriteService.updateFactoryStage(stageDTO)
                .thenAccept(stageOptional ->
                    Platform.runLater(() -> {
                        if (stageOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create stage.");
                            return;
                        }
                        FactoryStage stage = stageOptional.get();
                        fallbackManager.setLoading(false);

                        graphService.refreshFactoryGraph(factoryId).thenApply(productionGraphOptional -> {
                            if (productionGraphOptional.isEmpty()) {
                                fallbackManager.setErrorMessage("Failed to refresh factory graph");
                            }
                            if (actionListener != null) {
                                actionListener.onUpdateStage(productionGraphOptional.get());
                            }
                            return productionGraphOptional;
                        });
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private UpdateFactoryStageDTO getStageDTO() {
        UpdateFactoryStageDTO stageDTO = new UpdateFactoryStageDTO();

        stageDTO.setId(factoryStage.getId());
        stageDTO.setCapacity(parseFloat(capacityField.getText()));
        stageDTO.setDuration(selectDurationController.getTimeSeconds());
        stageDTO.setPriority(parseInt(priorityField.getText()));
        stageDTO.setMinimumRequiredCapacity(parseFloat(minimumRequiredCapacityField.getText()));

        return stageDTO;
    }

}
