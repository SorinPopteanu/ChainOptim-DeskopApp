package org.chainoptim.desktop.features.productpipeline.controller;

import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.productpipeline.model.Component;
import org.chainoptim.desktop.features.productpipeline.service.ComponentService;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ComponentController implements Initializable {

    // Services
    private final ComponentService componentService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final CommonViewsLoader commonViewsLoader;

    // State
    private final FallbackManager fallbackManager;
    private Component component;

    // FXML
    @FXML
    private Label componentName;
    @FXML
    private Label componentDescription;
    @FXML
    private StackPane fallbackContainer;

    @Inject
    public ComponentController(ComponentService componentService,
                               CommonViewsLoader commonViewsLoader,
                               NavigationService navigationService,
                               CurrentSelectionService currentSelectionService,
                               FallbackManager fallbackManager) {
        this.componentService = componentService;
        this.commonViewsLoader = commonViewsLoader;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commonViewsLoader.loadFallbackManager(fallbackContainer);

        Integer componentId = currentSelectionService.getSelectedId();
        if (componentId != null) {
            loadComponent(componentId);
        } else {
            System.out.println("Missing component id.");
            fallbackManager.setErrorMessage("Failed to load component: missing component ID.");
        }
    }

    private void loadComponent(Integer componentId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        componentService.getComponentById(componentId)
                .thenApply(this::handleComponentResponse)
                .exceptionally(this::handleComponentException);
    }

    private Result<Component> handleComponentResponse(Result<Component> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load component.");
                return;
            }
            this.component = result.getData();
            fallbackManager.setLoading(false);

            componentName.setText(component.getName());
            componentDescription.setText(component.getDescription());
        });

        return result;
    }

    private Result<Component> handleComponentException(Throwable ex) {
        Platform.runLater(() -> fallbackManager.setErrorMessage("Failed to load component."));
        return new Result<>();
    }

    @FXML
    private void handleEditComponent() {
        currentSelectionService.setSelectedId(component.getId());
        navigationService.switchView("Update-Component?id=" + component.getId(), true, null);
    }
}
