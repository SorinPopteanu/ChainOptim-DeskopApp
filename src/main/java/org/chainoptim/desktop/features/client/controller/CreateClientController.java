package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.scene.layout.StackPane;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateClientController implements Initializable {

    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private FormField<String> nameFormField;


    @Inject
    public CreateClientController(ClientWriteService clientWriteService,
                                  NavigationService navigationService,
                                  CurrentSelectionService currentSelectionService,
                                  ToastManager toastManager,
                                  FallbackManager fallbackManager,
                                  CommonViewsLoader commonViewsLoader) {
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);

        initializeFormFields();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, null, "Your input is not valid.");
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateClientDTO clientDTO = getCreateClientDTO(organizationId);
        if (clientDTO == null) return;

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        clientWriteService.createClient(clientDTO)
                .thenApply(this::handleCreateClientResponse)
                .exceptionally(this::handleCreateClientException);
    }

    private CreateClientDTO getCreateClientDTO(Integer organizationId) {
        CreateClientDTO clientDTO = new CreateClientDTO();
        try {
            clientDTO.setName(nameFormField.handleSubmit());
            clientDTO.setOrganizationId(organizationId);
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

    private Result<Client> handleCreateClientResponse(Result<Client> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to create client.", OperationOutcome.ERROR));
                return;
            }
            Client client = result.getData();
            fallbackManager.setLoading(false);
            toastManager.addToast(new ToastInfo(
                    "Success", "Client created successfully.", OperationOutcome.SUCCESS));

            currentSelectionService.setSelectedId(client.getId());
            navigationService.switchView("Client?id=" + client.getId(), true);
        });
        return result;
    }

    private Result<Client> handleCreateClientException(Throwable ex) {
        Platform.runLater(() ->
            toastManager.addToast(new ToastInfo(
                    "Error", "Failed to create client.", OperationOutcome.ERROR)));
        return new Result<>();
    }

}

