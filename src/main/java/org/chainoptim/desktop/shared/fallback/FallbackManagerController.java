package org.chainoptim.desktop.shared.fallback;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Data
public class FallbackManagerController {

    @FXML
    private StackPane fallbackContentHolder;

    private FallbackManager fallbackManager;

    @Inject
    public FallbackManagerController(FallbackManager fallbackManager) {
        this.fallbackManager = fallbackManager;
        setupChangeListeners();
    }

    private void setupChangeListeners() {
        fallbackManager.errorMessageProperty().addListener((obs, oldVal, newVal) -> updateView());
        fallbackManager.isLoadingProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("isLoading changed to: " + newVal);
            updateView();
        });
        fallbackManager.noOrganizationProperty().addListener((obs, oldVal, newVal) -> updateView());
        fallbackManager.noResultsProperty().addListener((obs, oldVal, newVal) -> updateView());
    }

    @FXML
    public void initialize() {
        updateView();
    }

    private void updateView() {
        String viewPath = determineViewPathBasedOnState();
        if (!viewPath.isEmpty()) {
            try {
                URL url = getClass().getResource(viewPath);
                if (url == null) {
                    return;
                }
                System.out.println("Update to view: " + url);
                FXMLLoader loader = new FXMLLoader(url);
                Node fallbackView = loader.load();
                if (viewPath == "/org/chainoptim/desktop/shared/fallback/ErrorFallbackView.fxml") {
                    ErrorFallbackController controller = loader.getController();
                    controller.initialize(fallbackManager.getErrorMessage());
                }
                fallbackContentHolder.getChildren().setAll(fallbackView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // No fallback
            fallbackContentHolder.getChildren().clear();
            fallbackContentHolder.setPrefSize(0, 0);
        }
    }
    private String determineViewPathBasedOnState() {
        if (!fallbackManager.getErrorMessage().isEmpty()) {
            return "/org/chainoptim/desktop/shared/fallback/ErrorFallbackView.fxml";
        } else if (fallbackManager.isLoading()) {
            return "/org/chainoptim/desktop/shared/fallback/LoadingFallbackView.fxml";
        } else if (fallbackManager.isNoOrganization()) {
            return "/org/chainoptim/desktop/shared/fallback/NoOrganizationFallbackView.fxml";
        } else if (fallbackManager.isNoResults()) {
            return "/org/chainoptim/desktop/shared/fallback/NoResultsFallbackView.fxml";
        }
        return "";
    }

}