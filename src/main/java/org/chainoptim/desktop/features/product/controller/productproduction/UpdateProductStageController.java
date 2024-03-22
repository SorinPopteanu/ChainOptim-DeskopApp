package org.chainoptim.desktop.features.product.controller.productproduction;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.TabsActionListener;
import org.chainoptim.desktop.features.productpipeline.dto.UpdateStageDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.features.productpipeline.service.StageService;
import org.chainoptim.desktop.features.productpipeline.service.StageWriteService;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
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
                .thenAccept(stageOptional -> {
                    if (stageOptional.isEmpty()) {
                        fallbackManager.setErrorMessage("Failed to load product stage");
                        return;
                    }
                    Stage stage = stageOptional.get();
                    System.out.println("Stage: " + stage);
                    Platform.runLater(() -> {
                        this.stage = stage;

                        stageNameField.setText(String.valueOf(stage.getName()));
                        stageDescriptionField.setText(String.valueOf(stage.getDescription()));
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
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
                .thenAccept(stageOptional ->
                    Platform.runLater(() -> {
                        if (stageOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create stage.");
                            return;
                        }
                        Stage stage = stageOptional.get();
                        fallbackManager.setLoading(false);

                        graphService.refreshProductGraph(productId).thenApply(productionGraphOptional -> {
                            if (productionGraphOptional.isEmpty()) {
                                fallbackManager.setErrorMessage("Failed to refresh product graph");
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

    private UpdateStageDTO getStageDTO() {
        UpdateStageDTO stageDTO = new UpdateStageDTO();

        stageDTO.setId(stage.getId());
        stageDTO.setName(stageNameField.getText());

        return stageDTO;
    }

}
