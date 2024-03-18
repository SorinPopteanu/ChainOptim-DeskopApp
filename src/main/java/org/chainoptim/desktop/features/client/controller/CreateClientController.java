package org.chainoptim.desktop.features.client.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.client.dto.CreateClientDTO;
import org.chainoptim.desktop.features.client.model.Client;
import org.chainoptim.desktop.features.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateClientController implements Initializable {

    private final ClientWriteService clientWriteService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;
    private final FallbackManager fallbackManager;

    @FXML
    private StackPane fallbackContainer;
    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;


    @Inject
    public CreateClientController(
            ClientWriteService clientWriteService,
            NavigationService navigationService,
            CurrentSelectionService currentSelectionService,
            FallbackManager fallbackManager,
            FXMLLoaderService fxmlLoaderService,
            ControllerFactory controllerFactory
    ) {
        this.clientWriteService = clientWriteService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFallbackManager();
    }

    private void loadFallbackManager() {
        Node fallbackView = fxmlLoaderService.loadView(
                "/org/chainoptim/desktop/shared/fallback/FallbackManagerView.fxml",
                controllerFactory::createController
        );
        fallbackContainer.getChildren().add(fallbackView);
    }

    @FXML
    private void handleSubmit() {
        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        fallbackManager.setLoading(true);

        Integer organizationId = currentUser.getOrganization().getId();

        CreateClientDTO clientDTO = new CreateClientDTO();
        clientDTO.setName(nameField.getText());
        clientDTO.setOrganizationId(organizationId);

        System.out.println(clientDTO);

        clientWriteService.createClient(clientDTO)
                .thenAccept(clientOptional -> {
                    Platform.runLater(() -> {
                        if (clientOptional.isEmpty()) {
                            fallbackManager.setErrorMessage("Failed to create client.");
                            return;
                        }
                        Client client = clientOptional.get();
                        fallbackManager.setLoading(false);
                        currentSelectionService.setSelectedId(client.getId());
                        navigationService.switchView("Client?id=" + client.getId());
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}

