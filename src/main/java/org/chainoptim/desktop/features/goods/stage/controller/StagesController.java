package org.chainoptim.desktop.features.goods.stage.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.stage.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.goods.stage.service.StageService;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.ListHeaderParams;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StagesController implements Initializable {

    // Services
    private final StageService stageService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;
    private long totalCount;
    private final Map<String, String> sortOptions = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    // Controllers
    private ListHeaderController headerController;
    private PageSelectorController pageSelectorController;

    // FXML
    @FXML
    private ScrollPane stagesScrollPane;
    @FXML
    private VBox stagesVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    @Inject
    public StagesController(StageService stageService,
                            NavigationService navigationService,
                            CurrentSelectionService currentSelectionService,
                            CommonViewsLoader commonViewsLoader,
                            FallbackManager fallbackManager,
                            SearchParams searchParams
    ) {
        this.stageService = stageService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Stages", "/img/box-solid.png", Feature.PRODUCT, sortOptions, null, this::loadStages, "Stage", "Create-Stage"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadStages();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        // Listen to changes in search params
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadStages());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadStages());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadStages());
        searchParams.getPageProperty().addListener((obs, oldPage, newPage) -> loadStages());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            stagesScrollPane.setVisible(newValue);
            stagesScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadStages() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        stageService.getStagesByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleStageResponse)
                .exceptionally(this::handleStageException);
    }

    private Result<PaginatedResults<StagesSearchDTO>> handleStageResponse(Result<PaginatedResults<StagesSearchDTO>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load stages.");
                return;
            }
            PaginatedResults<StagesSearchDTO> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int stagesLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxProductStages();
            headerController.disableCreateButton(stagesLimit != -1 && totalCount >= stagesLimit, "You have reached the limit of stages allowed by your current subscription plan.");

            stagesVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (StagesSearchDTO stage : paginatedResults.results) {
                loadStageCardUI(stage);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private void loadStageCardUI(StagesSearchDTO stage) {
        Label stageName = new Label(stage.getName());
        stageName.getStyleClass().add("entity-name-label");
        Label stageDescription = new Label("");
        stageDescription.getStyleClass().add("entity-description-label");
        VBox stageBox = new VBox(stageName, stageDescription);
        Button stageButton = new Button();
        stageButton.getStyleClass().add("entity-card");
        stageButton.setGraphic(stageBox);
        stageButton.setMaxWidth(Double.MAX_VALUE);
        stageButton.prefWidthProperty().bind(stagesVBox.widthProperty());
        stageButton.setOnAction(event -> openStageDetails(stage.getId()));
        stagesVBox.getChildren().add(stageButton);
    }

    private Result<PaginatedResults<StagesSearchDTO>> handleStageException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load stages."));
        return new Result<>();
    }

    private void openStageDetails(Integer stageId) {
        // Use currentSelectionService to remember the stageId
        // And also encode it in the viewKey for caching purposes
        currentSelectionService.setSelectedId(stageId);
        currentSelectionService.setSelectedPage("Stage");
        navigationService.switchView("Stage?id=" + stageId, true, null);
    }
}