package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientsControllerTest {

    @Mock
    private ClientService clientService;
    @Mock
    private NavigationService navigationService;
    @Mock
    private CurrentSelectionService currentSelectionService;
    @Mock
    private CommonViewsLoader commonViewsLoader;
    @Mock
    private FallbackManager fallbackManager;
    @Mock
    private SearchParams searchParams;
    @Mock
    private ListHeaderController headerController;
    @Mock
    private PageSelectorController pageSelectorController;

    @InjectMocks
    private ClientsController clientsController;

    @BeforeEach
    void setUp() {
        when(commonViewsLoader.loadListHeader(isNull())).thenReturn(headerController);
        when(commonViewsLoader.loadPageSelector(isNull())).thenReturn(pageSelectorController);

    }

//    @Test
//    void testInitialization() {
//        clientsController.initialize(null, null);
//
//        verify(commonViewsLoader).loadListHeader(isNull());
//        verify(commonViewsLoader).loadPageSelector(isNull());
//        verify(commonViewsLoader).loadFallbackManager(isNull());
//    }
}
