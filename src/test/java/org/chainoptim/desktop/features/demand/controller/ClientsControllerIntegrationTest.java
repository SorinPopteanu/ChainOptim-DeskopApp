package org.chainoptim.desktop.features.demand.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.demand.client.controller.ClientsController;
import org.chainoptim.desktop.features.demand.client.model.Client;
import org.chainoptim.desktop.features.demand.client.service.ClientService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParamsImpl;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasChildren;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(MockitoExtension.class)
class ClientsControllerIntegrationTest {

    @Mock
    private ClientService clientService;
    @Mock
    private NavigationService navigationService;
    @Mock
    private CurrentSelectionService currentSelectionService;
    @Mock
    private CommonViewsLoader commonViewsLoader;

    @InjectMocks
    private ClientsController clientsController;

    @BeforeEach
    void setUp() {
        clientsController = new ClientsController(clientService, navigationService, currentSelectionService, commonViewsLoader, new FallbackManager(), new SearchParamsImpl());

    }

    @Test
    void testLoadClients() {
        List<Client> receivedClients = List.of(new Client(1, "New Client", LocalDateTime.now(), LocalDateTime.now(), 1, new Location()));
        when(clientService.getClientsByOrganizationIdAdvanced(any(), any())).thenReturn(CompletableFuture.completedFuture(
                new Result<>(new PaginatedResults<>(receivedClients, 1), null, HttpURLConnection.HTTP_OK)));

        Platform.runLater(() -> clientsController.loadClients());

        verifyThat("#clientsVBox", hasChildren(1, ".entity-card"));
    }
}
