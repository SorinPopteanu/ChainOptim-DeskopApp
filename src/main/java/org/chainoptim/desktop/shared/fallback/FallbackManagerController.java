package org.chainoptim.desktop.shared.fallback;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
public class FallbackManagerController {

    @FXML
    private StackPane fallbackContentHolder;

    private FallbackManager fallbackManager;

    private Map<String, Node> loadedViews = new HashMap<>();
    private Map<String, Object> loadedControllers = new HashMap<>();

    @Inject
    public FallbackManagerController(FallbackManager fallbackManager) {
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize() {
        setupChangeListeners();
        updateView();
    }

    private void setupChangeListeners() {
        fallbackManager.errorMessageProperty().addListener((obs, oldVal, newVal) -> updateView());
        fallbackManager.isLoadingProperty().addListener((obs, oldVal, newVal) -> updateView());
        fallbackManager.noOrganizationProperty().addListener((obs, oldVal, newVal) -> updateView());
        fallbackManager.noResultsProperty().addListener((obs, oldVal, newVal) -> updateView());
    }

    private void updateView() {
        String viewPath = determineViewPathBasedOnState();

        if (viewPath.isEmpty()) { // No fallback
            fallbackContentHolder.getChildren().clear();
            fallbackContentHolder.setPrefSize(0, 0);
            return;
        }

        // Look in cache or load
        if (!loadedViews.containsKey(viewPath)) {
            loadFallbackView(viewPath);
        }
        Node view = loadedViews.get(viewPath);

        // Set error message in case of error
        Object controller = loadedControllers.get(viewPath);
        if (viewPath.equals("/org/chainoptim/desktop/shared/fallback/ErrorFallbackView.fxml")) {
            ((ErrorFallbackController)controller).initialize(fallbackManager.getErrorMessage());
        }

        fallbackContentHolder.getChildren().setAll(view);
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

    private void loadFallbackView(String viewPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Node view = loader.load();
            Object controller = loader.getController();

            loadedViews.put(viewPath, view);
            loadedControllers.put(viewPath, controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}