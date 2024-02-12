package org.chainoptim.desktop.core;

import javafx.scene.Group;
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
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApplication.class.getResource("/org/chainoptim/desktop/core/user/view/AuthView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 400);

        URL cssURL = MainApplication.class.getResource("/css/login.css");
        System.out.println("CSS URL: " + cssURL);

        if (cssURL != null) {
            String css = cssURL.toExternalForm();
            scene.getStylesheets().add(css);
        } else {
            System.out.println("CSS file not found");
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void loadMainScene() throws IOException {
        Parent root = FXMLLoader.load(MainApplication.class.getResource("/org/chainoptim/desktop/core/MainApplication.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("ChainOptim");
        primaryStage.show();
    }
}
