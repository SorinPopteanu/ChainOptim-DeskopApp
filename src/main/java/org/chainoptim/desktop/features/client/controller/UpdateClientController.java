package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.common.uielements.SelectOrCreateLocationController;
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

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateClientController implements Initializable {

    private final ClientService clientService;
    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
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
    public UpdateClientController(
            ClientService clientService,
            ClientWriteService clientWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.clientService = clientService;
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
        loadSelectOrCreateLocation();
        loadClient(currentSelectionService.getSelectedId());
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    private void loadSelectOrCreateLocation() {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/shared/common/uielements/SelectOrCreateLocationView.fxml",
                controllerFactory::createController
        );
        try {
            Node selectOrCreateLocationView = loader.load();
            selectOrCreateLocationController = loader.getController();
            selectOrCreateLocationContainer.getChildren().add(selectOrCreateLocationView);
            selectOrCreateLocationController.initialize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadClient(Integer clientId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientService.getClientById(clientId)
                .thenApply(this::handleClientResponse)
                .exceptionally(this::handleClientException)
                .thenRun(() -> Platform.runLater(() -> fallbackManager.setLoading(false)));
    }

    private Optional<Client> handleClientResponse(Optional<Client> clientOptional) {
        Platform.runLater(() -> {
            if (clientOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load client.");
                return;
            }
            client = clientOptional.get();

            nameField.setText(client.getName());
            selectOrCreateLocationController.setSelectedLocation(client.getLocation());
        });

        return clientOptional;
    }

    private Optional<Client> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client."));
        return Optional.empty();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        UpdateClientDTO clientDTO = getUpdateClientDTO();
        System.out.println(clientDTO);

        clientWriteService.updateClient(clientDTO)
                .thenAccept(clientOptional ->
                    Platform.runLater(() -> {
                        if (clientOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create client.");
                            return;
                        }
                        fallbackManager.setLoading(false);

                        // Manage navigation, invalidating previous client cache
                        Client updatedClient = clientOptional.get();
                        String clientPage = "Client?id=" + updatedClient.getId();
                        NavigationServiceImpl.invalidateViewCache(clientPage);
                        currentSelectionService.setSelectedId(updatedClient.getId());
                        navigationService.switchView(clientPage, true);
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
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

