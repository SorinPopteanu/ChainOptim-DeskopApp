package org.chainoptim.desktop.features.factory.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.service.FactoryService;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.ListHeaderParams;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FactoriesController implements Initializable {

    // Services
    private final FactoryService factoryService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final SearchParams searchParams;
    private final FallbackManager fallbackManager;
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
    private ScrollPane factoriesScrollPane;
    @FXML
    private VBox factoriesVBox;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane headerContainer;


    @Inject
    public FactoriesController(FactoryService factoryService,
                              NavigationService navigationService,
                              CurrentSelectionService currentSelectionService,
                              CommonViewsLoader commonViewsLoader,
                              FallbackManager fallbackManager,
                              SearchParams searchParams
    ) {
        this.factoryService = factoryService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Factories", "/img/industry-solid.png", Feature.FACTORY, sortOptions, null, this::loadFactories, "Factory", "Create-Factory"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadFactories();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        // Listen to changes in search params
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadFactories());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadFactories());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadFactories());
        searchParams.getPageProperty().addListener((obs, oldPage, newPage) -> loadFactories());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            factoriesScrollPane.setVisible(newValue);
            factoriesScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadFactories() {
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
                fallbackManager.setErrorMessage("Failed to load factories.");
                return;
            }
            PaginatedResults<Factory> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int factoriesLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxFactories();
            headerController.disableCreateButton(factoriesLimit != -1 && totalCount >= factoriesLimit, "You have reached the limit of factories allowed by your current subscription plan.");

            factoriesVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Factory factory : paginatedResults.results) {
                loadFactoryCardUI(factory);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private Result<PaginatedResults<Factory>> handleFactoryException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load factories."));
        return new Result<>();
    }

    private void loadFactoryCardUI(Factory factory) {
        Label factoryName = new Label(factory.getName());
        factoryName.getStyleClass().add("entity-name-label");
        Label factoryLocation = new Label();
        if (factory.getLocation() != null) {
            factoryLocation.setText(factory.getLocation().getFormattedLocation());
        } else {
            factoryLocation.setText("");
        }
        factoryLocation.getStyleClass().add("entity-description-label");

        VBox factoryBox = new VBox(factoryName, factoryLocation);
        Button factoryButton = new Button();
        factoryButton.getStyleClass().add("entity-card");
        factoryButton.setGraphic(factoryBox);
        factoryButton.setMaxWidth(Double.MAX_VALUE);
        factoryButton.prefWidthProperty().bind(factoriesVBox.widthProperty());
        factoryButton.setOnAction(event -> openFactoryDetails(factory.getId()));

        factoriesVBox.getChildren().add(factoryButton);
    }

    private void openFactoryDetails(Integer factoryId) {
        // Use currentSelectionService to remember the factoryId
        // And also encode it in the viewKey for caching purposes
        currentSelectionService.setSelectedId(factoryId);
        currentSelectionService.setSelectedPage("Factory");

        navigationService.switchView("Factory?id=" + factoryId, true, null);
    }
}
