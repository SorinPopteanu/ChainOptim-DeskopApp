package org.chainoptim.desktop.features.goods.component.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.shared.search.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.component.model.Component;
import org.chainoptim.desktop.features.goods.component.service.ComponentService;
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

public class ComponentsController implements Initializable {

    // Services
    private final ComponentService componentService;
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
    private ScrollPane componentsScrollPane;
    @FXML
    private VBox componentsVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    @Inject
    public ComponentsController(ComponentService componentService,
                                NavigationService navigationService,
                                CurrentSelectionService currentSelectionService,
                                CommonViewsLoader commonViewsLoader,
                                FallbackManager fallbackManager,
                                SearchParams searchParams
    ) {
        this.componentService = componentService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(new ListHeaderParams(null, searchParams, "Components", "/img/box-solid.png", Feature.PRODUCT, sortOptions, null, this::loadComponents, "Component", "Create-Component"));
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadComponents();
        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        // Listen to changes in search params
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadComponents());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadComponents());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadComponents());
        searchParams.getPageProperty().addListener((obs, oldPage, newPage) -> loadComponents());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            componentsScrollPane.setVisible(newValue);
            componentsScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadComponents() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        componentService.getComponentsByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleComponentResponse)
                .exceptionally(this::handleComponentException);
    }

    private Result<PaginatedResults<Component>> handleComponentResponse(Result<PaginatedResults<Component>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load components.");
                return;
            }
            PaginatedResults<Component> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int componentsLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxComponents();
            headerController.disableCreateButton(componentsLimit != -1 && totalCount >= componentsLimit, "You have reached the limit of components allowed by your current subscription plan.");

            componentsVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Component component : paginatedResults.results) {
                loadComponentCardUI(component);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private void loadComponentCardUI(Component component) {
        Label componentName = new Label(component.getName());
        componentName.getStyleClass().add("entity-name-label");
        Label componentDescription = new Label(component.getDescription());
        componentDescription.getStyleClass().add("entity-description-label");
        VBox componentBox = new VBox(componentName, componentDescription);
        Button componentButton = new Button();
        componentButton.getStyleClass().add("entity-card");
        componentButton.setGraphic(componentBox);
        componentButton.setMaxWidth(Double.MAX_VALUE);
        componentButton.prefWidthProperty().bind(componentsVBox.widthProperty());
        componentButton.setOnAction(event -> openComponentDetails(component.getId()));
        componentsVBox.getChildren().add(componentButton);
    }

    private Result<PaginatedResults<Component>> handleComponentException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load components."));
        return new Result<>();
    }

    private void openComponentDetails(Integer componentId) {
        // Use currentSelectionService to remember the componentId
        // And also encode it in the viewKey for caching purposes
        currentSelectionService.setSelectedId(componentId);
        currentSelectionService.setSelectedPage("Component");
        navigationService.switchView("Component?id=" + componentId, true, null);
    }
}