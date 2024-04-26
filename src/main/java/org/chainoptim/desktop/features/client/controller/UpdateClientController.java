package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.client.dto.UpdateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.features.product.model.Product;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
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

    // Services
    private final ClientService clientService;
    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // Controllers
    private SelectOrCreateLocationController selectOrCreateLocationController;

    // State
    private Client client;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;

    @Inject
    public UpdateClientController(
            ClientService clientService,
            ClientWriteService clientWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager) {
        this.clientService = clientService;
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
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

            initializeFormFields();
            selectOrCreateLocationController.setSelectedLocation(client.getLocation());
        });

        return result;
    }

    private Result<Client> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client."));
        return new Result<>();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, client.getName(), "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        UpdateClientDTO clientDTO = getUpdateClientDTO();
        if (clientDTO == null) return;
        System.out.println(clientDTO);

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientWriteService.updateClient(clientDTO)
                .thenApply(this::handleUpdateClientResponse)
                .exceptionally(this::handleUpdateClientException);
    }

    private UpdateClientDTO getUpdateClientDTO() {
        UpdateClientDTO clientDTO = new UpdateClientDTO();
        clientDTO.setId(client.getId());
        try {
            clientDTO.setName(nameFormField.handleSubmit());

            if (selectOrCreateLocationController.isCreatingNewLocation()) {
                clientDTO.setCreateLocation(true);
                clientDTO.setLocation(selectOrCreateLocationController.getNewLocationDTO());
            } else {
                clientDTO.setCreateLocation(false);
                clientDTO.setLocationId(selectOrCreateLocationController.getSelectedLocation().getId());
            }
        } catch (ValidationException e) {
            return null;
        }

        return clientDTO;
    }

    private Result<Client> handleUpdateClientResponse(Result<Client> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update client.", OperationOutcome.ERROR));
                return;
            }
            toastManager.addToast(new ToastInfo(
                    "Success", "Client updated successfully.", OperationOutcome.SUCCESS));

            // Manage navigation, invalidating previous client cache
            Client updatedClient = result.getData();
            String clientPage = "Client?id=" + updatedClient.getId();
            NavigationServiceImpl.invalidateViewCache(clientPage);
            currentSelectionService.setSelectedId(updatedClient.getId());
            navigationService.switchView(clientPage, true);
        });
        return result;
    }

    private Result<Client> handleUpdateClientException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update client.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

