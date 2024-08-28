package org.chainoptim.desktop.features.demand.client.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.client.service.ClientService;
import org.chainoptim.desktop.features.demand.client.service.ClientWriteService;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.common.ui.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.common.ui.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ClientController implements Initializable {

    // Services
    private final ClientService clientService;
    private final ClientWriteService clientWriteService;
    private final CommonViewsLoader commonViewsLoader;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;

    // Controllers
    private GenericConfirmDialogController<Client> confirmClientDeleteController;

    // Listeners
    private RunnableConfirmDialogActionListener<Client> confirmDialogDeleteListener;

    // State
    private final FallbackManager fallbackManager;
    private final ToastManager toastManager;
    private Client client;

    // FXML
    @FXML
    private Label clientName;
    @FXML
    private Label clientLocation;
    @FXML
    private Button deleteButton;
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
    private StackPane fallbackContainer;
    @FXML
    private StackPane confirmDeleteDialogContainer;

    @Inject
    public ClientController(ClientService clientService,
                            ClientWriteService clientWriteService,
                            CommonViewsLoader commonViewsLoader,
                            NavigationService navigationService,
                            CurrentSelectionService currentSelectionService,
                            FallbackManager fallbackManager,
                            ToastManager toastManager) {
        this.clientService = clientService;
        this.clientWriteService = clientWriteService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
        this.toastManager = toastManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        setupListeners();
        loadDeleteButton();
        loadComponents();

        Integer clientId = currentSelectionService.getSelectedId();
        if (clientId != null) {
            loadClient(clientId);
        } else {
            System.out.println("Missing client id.");
            fallbackManager.setErrorMessage("Failed to load client.");
        }
    }

    private void setupListeners() {
        setUpFallbackListeners();
        setUpTabListeners();
        setUpDialogListeners();
    }

    private void setUpFallbackListeners() {
        fallbackManager.isEmptyProperty().addListener((observable, oldValue, newValue) -> {
            tabPane.setVisible(newValue);
            tabPane.setManaged(newValue);
            fallbackContainer.setVisible(!newValue);
            fallbackContainer.setManaged(!newValue);
        });
    }

    private void setUpTabListeners() {
        overviewTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && overviewTab.getContent() == null) {
                commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/demand/ClientOverviewView.fxml", new SearchData<>(this.client, SearchMode.SECONDARY));
            }
        });
        ordersTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && ordersTab.getContent() == null) {
                commonViewsLoader.loadTabContent(ordersTab, "/org/chainoptim/desktop/features/demand/ClientOrdersView.fxml", new SearchData<>(this.client, SearchMode.SECONDARY));
            }
        });
        shipmentsTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && shipmentsTab.getContent() == null) {
                commonViewsLoader.loadTabContent(shipmentsTab, "/org/chainoptim/desktop/features/demand/ClientShipmentsView.fxml", new SearchData<>(this.client, SearchMode.SECONDARY));
            }
        });
        evaluationTab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (Boolean.TRUE.equals(isNowSelected) && evaluationTab.getContent() == null) {
                commonViewsLoader.loadTabContent(evaluationTab, "/org/chainoptim/desktop/features/demand/ClientEvaluationView.fxml", new SearchData<>(this.client, SearchMode.SECONDARY));
            }
        });
    }

    private void setUpDialogListeners() {
        Consumer<Client> onConfirmDelete = this::handleDeleteClient;
        Runnable onCancelDelete = this::closeConfirmDeleteDialog;
        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);
    }

    private void loadDeleteButton() {
        Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(14);
        deleteImageView.setFitHeight(14);
        deleteButton.setGraphic(deleteImageView);
        deleteButton.setTooltip(new Tooltip("Delete client"));
        deleteButton.setOnAction(event -> openConfirmDeleteDialog(client));
    }

    private void loadComponents() {
        confirmClientDeleteController = commonViewsLoader.loadConfirmDialog(confirmDeleteDialogContainer);
        confirmClientDeleteController.setActionListener(confirmDialogDeleteListener);
        closeConfirmDeleteDialog();
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
            commonViewsLoader.loadTabContent(overviewTab, "/org/chainoptim/desktop/features/demand/ClientOverviewView.fxml", this.client);
        });

        return result;
    }

    private Result<Client> handleClientException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load client."));
        return new Result<>();
    }

    // Actions
    @FXML
    private void handleEditClient() {
        currentSelectionService.setSelectedId(client.getId());
        navigationService.switchView("Update-Client?id=" + client.getId(), true, null);
    }

    private void openConfirmDeleteDialog(Client client) {
        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput(
                "Confirm Client Delete",
                "Are you sure you want to delete this client? This action cannot be undone.",
                null);
        confirmClientDeleteController.setData(client, confirmDialogInput);
        toggleDialogVisibility(confirmDeleteDialogContainer, true);
    }

    private void handleDeleteClient(Client client) {
        fallbackManager.setLoading(true);

        clientWriteService.deleteClient(client.getId())
                .thenApply(this::handleDeleteResponse)
                .exceptionally(this::handleDeleteException);
    }

    private Result<Integer> handleDeleteResponse(Result<Integer> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Failed to delete client.",
                        "An error occurred while deleting the client.",
                        OperationOutcome.ERROR)
                );
                return;
            }

            toastManager.addToast(new ToastInfo(
                    "Client deleted.",
                    "The Client \"" + client.getName() + "\" has been successfully deleted.",
                    OperationOutcome.SUCCESS)
            );

            NavigationServiceImpl.invalidateViewCache("Clients");
            navigationService.switchView("Clients", true, null);
        });
        return result;
    }

    private Result<Integer> handleDeleteException(Throwable ex) {
        Platform.runLater(() ->
                toastManager.addToast(new ToastInfo(
                        "Failed to delete client.",
                        "An error occurred while deleting the client.",
                        OperationOutcome.ERROR)
                )
        );
        return new Result<>();
    }

    private void closeConfirmDeleteDialog() {
        toggleDialogVisibility(confirmDeleteDialogContainer, false);
    }

    private void toggleDialogVisibility(StackPane dialogContainer, boolean isVisible) {
        dialogContainer.setVisible(isVisible);
        dialogContainer.setManaged(isVisible);
    }
}
