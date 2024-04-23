package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    private final ClientService clientService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private Client client;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab overviewTab;
    @FXML
    private Tab ordersTab;
    @FXML
    private Tab shipmentsTab;
    @FXML
    private Tab evaluationTab;
    @FXML
    private Label clientName;
    @FXML
    private Label clientLocation;

    @Inject
    public ClientController(ClientService clientService,
                            NavigationService navigationService,
                            CurrentSelectionService currentSelectionService,
                            CommonViewsLoader commonViewsLoader,
                            FallbackManager fallbackManager) {
        this.clientService = clientService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();

        Integer clientId = currentSelectionService.getSelectedId();
        if (clientId != null) {
            loadClient(clientId);
        } else {
            System.out.println("Missing client id.");
            fallbackManager.setErrorMessage("Failed to load client.");
        }
    }

    private void setupListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/client/ClientOverviewView.fxml", this.client);
            }
        });
        ordersTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && ordersTab.getContent() == null) {
                commonViewsLoader.loadTabContent(ordersTab, "/org/chainoptim/desktop/features/client/ClientOrdersView.fxml", this.client);
            }
        });
        shipmentsTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && shipmentsTab.getContent() == null) {
                commonViewsLoader.loadTabContent(shipmentsTab, "/org/chainoptim/desktop/features/client/ClientShipmentsView.fxml", this.client);
            }
        });
        evaluationTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && evaluationTab.getContent() == null) {
                commonViewsLoader.loadTabContent(evaluationTab, "/org/chainoptim/desktop/features/client/ClientEvaluationView.fxml", this.client);
            }
        });

        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void loadClient(Integer clientId) {
        fallbackManager.setLoading(true);

        clientService.getClientById(clientId)
                .thenApply(this::handleClientResponse)
                .exceptionally(this::handleClientException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Result<Client> handleClientResponse(Result<Client> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load client.");
                return;
            }
            this.client = result.getData();

            clientName.setText(client.getName());
            if (client.getLocation() != null) {
                clientLocation.setText(client.getLocation().getFormattedLocation());
            } else {
                clientLocation.setText("");
            }

            // Load overview tab
            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/client/ClientOverviewView.fxml", this.client);
        });

        return result;
    }

    private Result<Client> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client."));
        return new Result<>();
    }

    @FXML
    private void handleEditClient() {
        currentSelectionService.setSelectedId(client.getId());
        navigationService.switchView("Update-Client?id=" + client.getId(), true);
    }

}
