package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.shared.common.uielements.performance.ScoreDisplay;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.ListHeaderParams;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FactoryPerformancesController implements Initializable {

    // Services
    private final FactoryService factoryService;
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
    private ScrollPane factorysScrollPane;
    @FXML
    private VBox factorysVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    @Inject
    public FactoryPerformancesController(FactoryService factoryService,
                                         NavigationService navigationService,
                                         CurrentSelectionService currentSelectionService,
                                         CommonViewsLoader commonViewsLoader,
                                         FallbackManager fallbackManager,
                                         SearchParams searchParams) {
        this.factoryService = factoryService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Factorys", "/img/truck-arrow-right-solid.png", Feature.SUPPLIER, sortOptions, null, this::loadFactorys, "Factory", "Create-Factory"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadFactorys();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadFactorys());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadFactorys());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadFactorys());
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadFactorys());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            factorysScrollPane.setVisible(newValue);
            factorysScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadFactorys() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        factoryService.getFactoriesByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleFactoryResponse)
                .exceptionally(this::handleFactoryException);
    }

    private Result<PaginatedResults<Factory>> handleFactoryResponse(Result<PaginatedResults<Factory>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
               fallbackManager.setErrorMessage("Failed to load factorys.");
               return;
            }
            PaginatedResults<Factory> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int factorysLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxFactories();
            headerController.disableCreateButton(factorysLimit != -1 && totalCount >= factorysLimit, "You have reached the limit of factorys allowed by your current subscription plan.");

            factorysVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Factory factory : paginatedResults.results) {
                drawFactoryCardUI(factory);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private Result<PaginatedResults<Factory>> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factorys."));
        return new Result<>();
    }

    private void drawFactoryCardUI(Factory factory) {
        HBox cardHBox = new HBox();
        cardHBox.setAlignment(Pos.CENTER);

        // Left side
        VBox factoryBox = new VBox();
        cardHBox.getChildren().add(factoryBox);

        Label factoryName = new Label(factory.getName());
        factoryName.getStyleClass().add("entity-name-label");
        factoryBox.getChildren().add(factoryName);
        Label factoryLocation = new Label();
        if (factory.getLocation() != null) {
            factoryLocation.setText(factory.getLocation().getFormattedLocation());
        } else {
            factoryLocation.setText("");
        }
        factoryLocation.getStyleClass().add("entity-description-label");
        factoryBox.getChildren().add(factoryLocation);

        // Separator
        Region separator = new Region();
        HBox.setHgrow(separator, Priority.ALWAYS);
        cardHBox.getChildren().add(separator);

        // Right side
        HBox factoryScoresHBox = new HBox(8);
        cardHBox.getChildren().add(factoryScoresHBox);
        drawFactoryScores(factory, factoryScoresHBox);

        Button factoryButton = new Button();
        factoryButton.getStyleClass().add("entity-card");
        factoryButton.setGraphic(cardHBox);
        factoryButton.setMaxWidth(Double.MAX_VALUE);
        factoryButton.prefWidthProperty().bind(factorysVBox.widthProperty());
        factoryButton.setOnAction(event -> openFactoryDetails(factory.getId()));

        factorysVBox.getChildren().add(factoryButton);
    }

    private void drawFactoryScores(Factory factory, HBox factoryScoresHBox) {
        addScore(factory.getOverallScore(), factoryScoresHBox, "Overall Score");
        addScore(factory.getTimelinessScore(), factoryScoresHBox, "Timeliness Score");
        addScore(factory.getAvailabilityScore(), factoryScoresHBox, "Availability Score");
        addScore(factory.getQualityScore(), factoryScoresHBox, "Quality Score");
    }

    private void addScore(Float score, HBox factoryScoresHBox, String tooltipText) {
        ScoreDisplay scoreDisplay = new ScoreDisplay();
        int scoreValue = score != null ? Math.round(score) : 0;
        scoreDisplay.setScore(scoreValue);
        scoreDisplay.setTooltipText(tooltipText);
        factoryScoresHBox.getChildren().add(scoreDisplay);
    }

    private void openFactoryDetails(Integer factoryId) {
        currentSelectionService.setSelectedId(factoryId);
        currentSelectionService.setSelectedPage("Factory");

        navigationService.switchView("Factory?id=" + factoryId, true, null);
    }
}

