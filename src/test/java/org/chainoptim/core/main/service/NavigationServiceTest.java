package org.chainoptim.core.main.service;

import javafx.scene.layout.Pane;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NavigationServiceTest {

    private NavigationServiceImpl navigationService;
    @Mock
    private StackPane mockMainContentArea;
    @Mock
    private FXMLLoaderService mockFxmlLoaderService;

    @BeforeEach
    void setUp() {
        when(mockFxmlLoaderService.loadView(anyString(), any())).thenReturn(mock(Pane.class));
        navigationService = new NavigationServiceImpl(mockFxmlLoaderService);
        navigationService.setMainContentArea(mockMainContentArea);
    }

    @Test
    void switchViewCacheWorks() {
        navigationService.switchView("Overview?id=1");
        navigationService.switchView("Overview?id=2");

        Assertions.assertTrue(NavigationServiceImpl.getViewCache().containsKey("Overview?id=1"));
        Assertions.assertTrue(NavigationServiceImpl.getViewCache().containsKey("Overview?id=2"));
    }
}
