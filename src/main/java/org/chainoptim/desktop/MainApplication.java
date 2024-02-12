package org.chainoptim.desktop;

import org.chainoptim.desktop.core.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationService;
import org.chainoptim.desktop.core.user.util.TokenManager;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager.setPrimaryStage(primaryStage);

        String jwtToken = TokenManager.getToken();
        boolean isTokenValid = jwtToken != null && AuthenticationService.validateJWTToken(jwtToken);

        // Show Login Scene or Main Scene depending on whether a valid JWT token exists
        if (isTokenValid) {
            SceneManager.loadMainScene();
        } else {
            SceneManager.loadLoginScene();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}