package org.chainoptim.desktop.features.goods.product.controller.productproduction;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.product.model.TabsActionListener;
import org.chainoptim.desktop.features.goods.stage.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.goods.stage.model.Stage;
import org.chainoptim.desktop.features.goods.stage.service.StageService;
import org.chainoptim.desktop.features.goods.stage.service.StageWriteService;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;

public class UpdateProductStageController {

    private final StageService stageService;
    private final StageWriteService stageWriteService;
    private final ProductProductionGraphService graphService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    private Stage stage;
    private Integer productId;

    @Setter
    private TabsActionListener actionListener;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private TextField stageNameField;
    @FXML
    private TextField stageDescriptionField;


    @Inject
    public UpdateProductStageController(
            StageService stageService,
            StageWriteService stageWriteService,
            ProductProductionGraphService graphService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.stageService = stageService;
        this.stageWriteService = stageWriteService;
        this.graphService = graphService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    public void initialize(Integer stageId, Integer productId) {
        this.productId = productId;
        loadFallbackManager();
        loadStageIntoForm(stageId);
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadStageIntoForm(Integer stageId) {
        stageService.getStageById(stageId)
                .thenApply(this::handleStageResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private Result<Stage> handleStageResponse(Result<Stage> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load product stage");
                return;
            }
            this.stage = result.getData();
            System.out.println("Stage: " + stage);

            stageNameField.setText(String.valueOf(stage.getName()));
            stageDescriptionField.setText(String.valueOf(stage.getDescription()));
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

        UpdateStageDTO stageDTO = getStageDTO();

        System.out.println(stageDTO);

        stageWriteService.updateStage(stageDTO)
                .thenApply(this::handleUpdateStageResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<Stage> handleUpdateStageResponse(Result<Stage> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create stage.");
                return;
            }
            Stage stage = result.getData();
            fallbackManager.setLoading(false);

            graphService.refreshProductGraph(productId)
                    .thenApply(graphResult -> {
                        if (graphResult.getError() != null) {
                            fallbackManager.setErrorMessage("Failed to refresh product graph");
                        }
                        if (actionListener != null && graphResult.getData() != null) {
                            actionListener.onUpdateStage(graphResult.getData());
                        }
                        return graphResult;
                    });
        });
        return result;
    }

    private UpdateStageDTO getStageDTO() {
        UpdateStageDTO stageDTO = new UpdateStageDTO();

        stageDTO.setId(stage.getId());
        stageDTO.setName(stageNameField.getText());

        return stageDTO;
    }

}
