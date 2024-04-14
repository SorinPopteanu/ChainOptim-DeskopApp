package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.common.uielements.select.SelectOrCreateLocationController;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.scene.layout.StackPane;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateClientController implements Initializable {

    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;
    private final FallbackManager fallbackManager;

    private SelectOrCreateLocationController selectOrCreateLocationController;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private StackPane selectOrCreateLocationContainer;
    @FXML
    private TextField nameField;


    @Inject
    public CreateClientController(
            ClientWriteService clientWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            CommonViewsLoader commonViewsLoader
    ) {
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.commonViewsLoader = commonViewsLoader;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        selectOrCreateLocationController = commonViewsLoader.loadSelectOrCreateLocation(selectOrCreateLocationContainer);
        selectOrCreateLocationController.initialize();
    }

    @FXML
    private void handleSubmit() {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Integer organizationId = currentUser.getOrganization().getId();

        CreateClientDTO clientDTO = getCreateClientDTO(organizationId);

        clientWriteService.createClient(clientDTO)
                .thenAccept(clientOptional ->
                    Platform.runLater(() -> {
                        if (clientOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create client.");
                            return;
                        }
                        Client client = clientOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(client.getId());
                        navigationService.switchView("Client?id=" + client.getId(), true);
                    })
                )
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private CreateClientDTO getCreateClientDTO(Integer organizationId) {
        CreateClientDTO clientDTO = new CreateClientDTO();
        clientDTO.setName(nameField.getText());
        clientDTO.setOrganizationId(organizationId);
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

