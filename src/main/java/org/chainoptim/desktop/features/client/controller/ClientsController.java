package org.chainoptim.desktop.features.client.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientsController implements Initializable {

    private final ClientService clientService;
    private final NavigationServiceImpl navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;
    private final SearchParams searchParams;

    @FXML
    private ListHeaderController headerController;
    @FXML
    private PageSelectorController pageSelectorController;
    @FXML
    private StackPane pageSelectorContainer;
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane headerContainer;
    @FXML
    private VBox clientsVBox;

    private long totalCount;

    private final Map<String, String> sortOptions = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    @Inject
    public ClientsController(ClientService clientService,
                             NavigationServiceImpl navigationService,
                             CurrentSelectionService currentSelectionService,
                             FXMLLoaderService fxmlLoaderService,
                             ControllerFactory controllerFactory,
                             FallbackManager fallbackManager,
                             SearchParams searchParams
   ) {
        this.clientService = clientService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
        this.searchParams = searchParams;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeHeader();
        loadFallbackManager();
        loadClients();
        setUpListeners();
        initializePageSelector();
    }

    private void initializePageSelector() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/search/PageSelectorView.fxml",
                controllerFactory::createController
        );
        try {
            Node pageSelectorView = loader.load();
            pageSelectorContainer.getChildren().add(pageSelectorView);
            pageSelectorController = loader.getController();
            searchParams.getPageProperty().addListener((observable, oldPage, newPage) -> loadClients());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeHeader() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/main/ListHeaderView.fxml",
                controllerFactory::createController
        );
        try {
            Node headerView = loader.load();
            headerContainer.getChildren().add(headerView);
            headerController = loader.getController();
            headerController.initializeHeader("Clients", "/img/truck-arrow-right-solid.png", sortOptions, "Client", "Create-Client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void setUpListeners() {
        searchParams.getSearchQueryProperty().addListener((observable, oldValue, newValue) -> loadClients());
        searchParams.getAscendingProperty().addListener((observable, oldValue, newValue) -> loadClients());
        searchParams.getSortOptionProperty().addListener((observable, oldValue, newValue) -> loadClients());
    }

    private void loadClients() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> fallbackManager.setLoading(false));
            return;
        }

        fallbackManager.setLoading(true);

        Integer organizationId = currentUser.getOrganization().getId();
        clientService.getClientsByOrganizationIdAdvanced(organizationId, searchParams)
                .thenApply(this::handleClientResponse)
                .exceptionally(this::handleClientException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<PaginatedResults<Client>> handleClientResponse(Optional<PaginatedResults<Client>> clientsOptional) {
        Platform.runLater(() -> {
           if (clientsOptional.isEmpty()) {
               fallbackManager.setErrorMessage("Failed to load clients.");
               return;
           }
           clientsVBox.getChildren().clear();
           PaginatedResults<Client> paginatedResults = clientsOptional.get();
           totalCount = paginatedResults.getTotalCount();

           if (!paginatedResults.results.isEmpty()) {
               for (Client client : paginatedResults.results) {
                   loadClientCardUI(client);
                   Platform.runLater(() -> pageSelectorController.initialize(totalCount));
               }
               fallbackManager.setNoResults(false);
           } else {
               fallbackManager.setNoResults(true);
           }
        });
        return clientsOptional;
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

    private Optional<PaginatedResults<Client>> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load clients."));
        return Optional.empty();
    }

    private void openClientDetails(Integer clientId) {
        System.out.println("Client about to be initialized");
        currentSelectionService.setSelectedId(clientId);
        currentSelectionService.setSelectedPage("Client");

        navigationService.switchView("Client?id=" + clientId);
    }







}

