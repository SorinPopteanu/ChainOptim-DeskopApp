package org.chainoptim.desktop.core.main.service;

import com.google.inject.Injector;
import lombok.Setter;
import org.chainoptim.desktop.MainApplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/*
 * Manager of current scene.
 * Currently switches between MainApplicationScene and LoginScene
 */
public class SceneManager {
    @Setter
    private static Stage primaryStage;

    private static Injector injector;

    public static void setInjector(Injector injector) {
        SceneManager.injector = injector;
    }

    public static void loadLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/org/chainoptim/desktop/core/user/AuthView.fxml"));
        // Set the controller factory to use Guice for DI
        loader.setControllerFactory(injector::getInstance);
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
        loader.setControllerFactory(injector::getInstance);
        Parent root = loader.load();
        Scene mainScene = new Scene(root, 800, 600);
        applyCss(mainScene, "/css/sidebar.css");
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("ChainOptim");
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
