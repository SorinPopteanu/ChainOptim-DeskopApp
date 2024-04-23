package org.chainoptim.desktop.features.client.controller;

import org.chainoptim.desktop.core.main.controller.ListHeaderController;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.client.service.ClientService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.search.controller.PageSelectorController;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
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
    private ClientsController controller;

    @BeforeEach
    void setUp() {
        when(commonViewsLoader.loadListHeader(any(StackPane.class))).thenReturn(headerController);
        when(commonViewsLoader.loadPageSelector(any(StackPane.class))).thenReturn(pageSelectorController);

    }

    @Test
    void testInitialization() {
        controller.initialize(null, null);

        verify(commonViewsLoader).loadListHeader(any(StackPane.class));
        verify(commonViewsLoader).loadPageSelector(any(StackPane.class));
        verify(commonViewsLoader).loadFallbackManager(any(StackPane.class));
    }
}
