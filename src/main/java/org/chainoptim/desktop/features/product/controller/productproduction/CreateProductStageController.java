package org.chainoptim.desktop.features.product.controller.productproduction;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.model.TabsActionListener;
import org.chainoptim.desktop.features.productpipeline.dto.CreateStageDTO;
import org.chainoptim.desktop.features.productpipeline.model.Stage;
import org.chainoptim.desktop.features.productpipeline.service.StageWriteService;
import org.chainoptim.desktop.features.scanalysis.productgraph.service.ProductProductionGraphService;
import org.chainoptim.desktop.shared.common.uielements.SelectFactoryController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateProductStageController implements Initializable {

    private final StageWriteService stageWriteService;
    private final ProductProductionGraphService graphService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @Setter
    private TabsActionListener actionListener;

    @FXML
    private StackPane fallbackContainer;

    @FXML
    private StackPane selectFactoryContainer;
    private SelectFactoryController selectFactoryController;
    @FXML
    private TextField nameField;

    @Inject
    public CreateProductStageController(
            StageWriteService stageWriteService,
            ProductProductionGraphService graphService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.stageWriteService = stageWriteService;
        this.graphService = graphService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectFactoryView();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
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

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        CreateStageDTO stageDTO = getStageDTO();
        System.out.println(stageDTO);

        stageWriteService.createStage(stageDTO)
                .thenAccept(stageOptional ->
                    Platform.runLater(() -> {
                        if (stageOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create stage.");
                            return;
                        }
                        Stage stage = stageOptional.get();
                        fallbackManager.setLoading(false);

                        graphService.refreshProductGraph(stageDTO.getProductId()).thenApply(productionGraphOptional -> {
                            if (productionGraphOptional.isEmpty()) {
                                fallbackManager.setErrorMessage("Failed to refresh product graph");
                            }
                            if (actionListener != null) {
                                actionListener.onAddStage(productionGraphOptional.get());
                            }
                            return productionGraphOptional;
                        });
                    })
                )
                .exceptionally(ex -> {
                    fallbackManager.setErrorMessage("Failed to create stage");
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateStageDTO getStageDTO() {
        CreateStageDTO stageDTO = new CreateStageDTO();

        Integer factoryId = selectFactoryController.getSelectedFactory().getId();
        stageDTO.setProductId(factoryId);

        stageDTO.setName(nameField.getText());
        return stageDTO;
    }

}
