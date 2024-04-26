package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
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

public class ClientsController implements Initializable {

    // Services
    private final ClientService clientService;
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

    @FXML
    private ScrollPane clientsScrollPane;
    @FXML
    private VBox clientsVBox;
    @FXML
    private StackPane headerContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane pageSelectorContainer;

    @Inject
    public ClientsController(ClientService clientService,
                             NavigationService navigationService,
                             CurrentSelectionService currentSelectionService,
                             CommonViewsLoader commonViewsLoader,
                             FallbackManager fallbackManager,
                             SearchParams searchParams) {
        this.clientService = clientService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerController = commonViewsLoader.loadListHeader(headerContainer);
        headerController.initializeHeader(searchParams, "Clients", "/img/truck-arrow-right-solid.png", Feature.CLIENT, sortOptions, this::loadClients, "Client", "Create-Client");
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setUpListeners();
        loadClients();

        pageSelectorController = commonViewsLoader.loadPageSelector(pageSelectorContainer);
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadClients());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadClients());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadClients());
        searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadClients());

        // Listen to empty fallback state
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            clientsScrollPane.setVisible(newValue);
            clientsScrollPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    public void loadClients() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        clientService.getClientsByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleClientResponse)
                .exceptionally(this::handleClientException);
    }

    private Result<PaginatedResults<Client>> handleClientResponse(Result<PaginatedResults<Client>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
               fallbackManager.setErrorMessage("Failed to load clients.");
               return;
            }
            PaginatedResults<Client> paginatedResults = result.getData();
            fallbackManager.setLoading(false);

            totalCount = paginatedResults.getTotalCount();
            pageSelectorController.initialize(searchParams, totalCount);
            int clientsLimit = TenantContext.getCurrentUser().getOrganization().getSubscriptionPlan().getMaxClients();
            headerController.disableCreateButton(clientsLimit != -1 && totalCount >= clientsLimit, "You have reached the limit of clients allowed by your current subscription plan.");

            clientsVBox.getChildren().clear();
            if (paginatedResults.results.isEmpty()) {
                fallbackManager.setNoResults(true);
                return;
            }

            for (Client client : paginatedResults.results) {
                loadClientCardUI(client);
            }
            fallbackManager.setNoResults(false);
        });
        return result;
    }

    private Result<PaginatedResults<Client>> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load clients."));
        return new Result<>();
    }

    private void loadClientCardUI(Client client) {
        Label clientName = new Label(client.getName());
        clientName.getStyleClass().add("entity-name-label");
        Label clientLocation = new Label();
        if (client.getLocation() != null) {
            clientLocation.setText(client.getLocation().getFormattedLocation());
        } else {
            clientLocation.setText("");
        }
        clientLocation.getStyleClass().add("entity-description-label");

        VBox clientBox = new VBox(clientName, clientLocation);
        Button clientButton = new Button();
        clientButton.getStyleClass().add("entity-card");
        clientButton.setGraphic(clientBox);
        clientButton.setMaxWidth(Double.MAX_VALUE);
        clientButton.prefWidthProperty().bind(clientsVBox.widthProperty());
        clientButton.setOnAction(event -> openClientDetails(client.getId()));

        clientsVBox.getChildren().add(clientButton);
    }

    private void openClientDetails(Integer clientId) {
        System.out.println("Client about to be initialized");
        currentSelectionService.setSelectedId(clientId);
        currentSelectionService.setSelectedPage("Client");

        navigationService.switchView("Client?id=" + clientId, true);
    }
}

