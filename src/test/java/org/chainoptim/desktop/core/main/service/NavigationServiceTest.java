package org.chainoptim.desktop.core.main.service;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.abstraction.ThreadRunner;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import javafx.scene.layout.Pane;
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

/*
 * Not functional
 */
@ExtendWith(MockitoExtension.class)
public class NavigationServiceTest {

    private NavigationServiceImpl navigationService;
    @Mock
    private StackPane mockMainContentArea;

    @BeforeEach
    void setUp() {
        ControllerFactory mockControllerFactory = mock(ControllerFactory.class);
        ThreadRunner mockThreadRunner = mock(ThreadRunner.class);
        FXMLLoaderService mockFxmlLoaderService = mock(FXMLLoaderService.class);
        when(mockFxmlLoaderService.loadView(anyString(), any())).thenReturn(new Pane());

        navigationService = new NavigationServiceImpl(mockFxmlLoaderService, mockControllerFactory, mockThreadRunner);
        navigationService.setMainContentArea(mockMainContentArea);
    }

    @Test
    void switchViewCacheWorks() {
        navigationService.switchView("Products?id=1");
        navigationService.switchView("Products?id=2");

        Assertions.assertTrue(NavigationServiceImpl.getViewCache().containsKey("Products?id=1"));
        Assertions.assertTrue(NavigationServiceImpl.getViewCache().containsKey("Products?id=2"));
    }
}
