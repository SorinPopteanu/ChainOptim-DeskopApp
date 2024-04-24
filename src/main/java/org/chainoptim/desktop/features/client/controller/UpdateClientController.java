package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateClientController implements Initializable {

    private final ClientService clientService;
    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private Client client;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;

    @Inject
    public UpdateClientController(ClientService clientService,
                                  ClientWriteService clientWriteService,
                                  NavigationService navigationService,
                                  CurrentSelectionService currentSelectionService,
                                  CommonViewsLoader commonViewsLoader,
                                  FallbackManager fallbackManager) {
        this.clientService = clientService;
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
        loadClient(currentSelectionService.getSelectedId());
    }

    private void loadClient(Integer clientId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientService.getClientById(clientId)
                .thenApply(this::handleClientResponse)
                .exceptionally(this::handleClientException);
    }

    private Result<Client> handleClientResponse(Result<Client> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load client.");
                return;
            }
            client = result.getData();
            fallbackManager.setLoading(false);

            nameField.setText(client.getName());
            selectOrCreateLocationController.setSelectedLocation(client.getLocation());
        });

        return result;
    }

    private Result<Client> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client."));
        return new Result<>();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateClientDTO clientDTO = getUpdateClientDTO();
        System.out.println(clientDTO);

        clientWriteService.updateClient(clientDTO)
                .thenApply(this::handleUpdateClientResponse)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new Result<>();
                });
    }

    private Result<Client> handleUpdateClientResponse(Result<Client> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to create client.");
                return;
            }
            fallbackManager.setLoading(false);

            // Manage navigation, invalidating previous client cache
            Client updatedClient = result.getData();
            String clientPage = "Client?id=" + updatedClient.getId();
            NavigationServiceImpl.invalidateViewCache(clientPage);
            currentSelectionService.setSelectedId(updatedClient.getId());
            navigationService.switchView(clientPage, true);
        });
        return result;
    }

    private UpdateClientDTO getUpdateClientDTO() {
        UpdateClientDTO clientDTO = new UpdateClientDTO();
        clientDTO.setId(client.getId());
        clientDTO.setName(nameField.getText());

        if (selectOrCreateLocationController.isCreatingNewLocation()) {
            clientDTO.setCreateLocation(true);
            clientDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
        } else {
            clientDTO.setCreateLocation(false);
            clientDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
        }

        return clientDTO;
    }
}

