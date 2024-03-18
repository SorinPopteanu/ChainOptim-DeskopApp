package org.chainoptim.desktop.core.main.service;

import com.google.inject.Injector;
import javafx.scene.image.Image;
import lombok.Setter;
import org.chainoptim.desktop.MainApplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/*
 * Manager of current scene.
 * Currently switches between MainApplicationScene and LoginScene
 */
public class SceneManager {
    @Setter
    private static Stage primaryStage;

    public static void loadLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/org/chainoptim/desktop/core/user/AuthView.fxml"));
        // Set the controller factory to use Guice for DI
        loader.setControllerFactory(MainApplication.injector::getInstance);
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 400);
        applyCss(scene, "/css/login.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void loadMainScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/org/chainoptim/desktop/core/main/AppView.fxml"));
        // Set the controller factory to use Guice for DI
        loader.setControllerFactory(MainApplication.injector::getInstance);
        Parent root = loader.load();
        Scene mainScene = new Scene(root, 1080, 720);
        applyCss(mainScene, "/css/globals.css");
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("ChainOptim");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(SceneManager.class.getResourceAsStream("/img/settings.png"))));

        primaryStage.show();
    }

    private static void applyCss(Scene scene, String cssPath) {
        URL cssURL = MainApplication.class.getResource(cssPath);
        if (cssURL != null) {
            String css = cssURL.toExternalForm();
            scene.getStylesheets().add(css);
        } else {
            System.out.println("CSS file not found");
        }
    }
}
