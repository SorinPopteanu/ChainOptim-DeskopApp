package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.UpdateFactoryStageDTO;
import org.chainoptim.desktop.features.factory.model.FactoryStage;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
import org.chainoptim.desktop.features.factory.service.FactoryStageService;
import org.chainoptim.desktop.features.factory.service.FactoryStageWriteService;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.FactoryProductionGraphService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectDurationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class UpdateFactoryStageController {

    private final FactoryStageService factoryStageService;
    private final FactoryStageWriteService factoryStageWriteService;
    private final FactoryProductionGraphService graphService;
    private final CommonViewsLoader commonViewsLoader;
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
            CommonViewsLoader commonViewsLoader,
            FallbackManager fallbackManager
    ) {
        this.factoryStageService = factoryStageService;
        this.factoryStageWriteService = factoryStageWriteService;
        this.graphService = graphService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(Integer factoryStageId, Integer factoryId) {
        this.factoryId = factoryId;
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        loadFactoryStageIntoForm(factoryStageId);
        selectDurationController = commonViewsLoader.loadSelectDurationView(durationInputContainer);
    }

    private void loadFactoryStageIntoForm(Integer factoryStageId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryStageService.getFactoryStageById(factoryStageId)
                .thenApply(this::handleFactoryStageResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<FactoryStage> handleFactoryStageResponse(Result<FactoryStage> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load factory stage");
                return;
            }
            this.factoryStage = result.getData();
            System.out.println("Factory stage: " + factoryStage);
            fallbackManager.setLoading(false);

            stageNameLabel.setText("Stage: " + factoryStage.getStage().getName());
            capacityField.setText(String.valueOf(factoryStage.getCapacity()));
            selectDurationController.setTime(factoryStage.getDuration());
            priorityField.setText(String.valueOf(factoryStage.getPriority()));
            minimumRequiredCapacityField.setText(String.valueOf(factoryStage.getMinimumRequiredCapacity()));
        });
        return result;
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
                .thenApply(this::handleUpdateFactoryStageResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<FactoryStage> handleUpdateFactoryStageResponse(Result<FactoryStage> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create stage.");
                return;
            }
            FactoryStage stage = result.getData();
            fallbackManager.setLoading(false);

            graphService.refreshFactoryGraph(factoryId).thenApply(graphResult -> {
                if (graphResult.getError() != null) {
                    fallbackManager.setErrorMessage("Failed to refresh factory graph");
                }
                if (actionListener != null && graphResult.getData() != null) {
                    actionListener.onUpdateStage(graphResult.getData());
                }
                return graphResult;
            });
        });
        return result;
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
