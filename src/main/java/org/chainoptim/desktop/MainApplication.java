package org.chainoptim.desktop;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.chainoptim.desktop.core.main.service.SceneManager;
import org.chainoptim.desktop.core.user.service.AuthenticationServiceImpl;
import org.chainoptim.desktop.core.user.util.TokenManager;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {

    public static Injector injector;

    private AuthenticationServiceImpl authenticationService;

    // Create Guice injector with AppModule
    @Override
    public void init() throws Exception {
        injector = Guice.createInjector(new AppModule());
        authenticationService = injector.getInstance(AuthenticationServiceImpl.class);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager.setPrimaryStage(primaryStage);

        // Show Login Scene or Main Scene depending on whether a valid JWT token exists
        String jwtToken = TokenManager.getToken();
        boolean isTokenValid = jwtToken != null && authenticationService.validateJWTToken(jwtToken);

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