package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.CreateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.factory.service.FactoryStageWriteService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectDurationController;
import org.chainoptim.desktop.shared.common.uielements.select.SelectFactoryController;
import org.chainoptim.desktop.shared.common.uielements.select.SelectStageController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import lombok.Setter;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class CreateFactoryStageController implements Initializable {

    // Services
    private final FactoryStageWriteService factoryStageWriteService;
    private final FactoryProductionGraphService graphService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final CommonViewsLoader commonViewsLoader;

    // Listeners
    @Setter
    private TabsActionListener actionListener;

    // State
    private final FallbackManager fallbackManager;

    // FXML
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
            ControllerFactory controllerFactory,
            CommonViewsLoader commonViewsLoader
    ) {
        this.factoryStageWriteService = factoryStageWriteService;
        this.graphService = graphService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectStageController = commonViewsLoader.loadSelectStageView(selectStageContainer);
        selectStageController.initialize();
        selectFactoryController = commonViewsLoader.loadSelectFactoryView(selectFactoryContainer);
        selectFactoryController.initialize();
        selectDurationController = commonViewsLoader.loadSelectDurationView(durationInputContainer);
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

        factoryStageWriteService.createFactoryStage(stageDTO, true)
                .thenApply(this::handleCreateStageResponse)
                .exceptionally(this::handleCreateStageException);
    }

    private Optional<FactoryStage> handleCreateStageResponse(Optional<FactoryStage> stageOptional) {
        Platform.runLater(() -> {
            if (stageOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to create stage.");
                return;
            }
            FactoryStage stage = stageOptional.get();
            fallbackManager.setLoading(false);

            graphService.refreshFactoryGraph(selectFactoryController.getSelectedFactory().getId())
                    .thenApply(productionGraphOptional -> {
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
        });
        return stageOptional;
    }

    private Optional<FactoryStage> handleCreateStageException(Throwable ex) {
        Platform.runLater(() -> {
            fallbackManager.setErrorMessage("Failed to create stage.");
            fallbackManager.setLoading(false);
        });
        return Optional.empty();
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
