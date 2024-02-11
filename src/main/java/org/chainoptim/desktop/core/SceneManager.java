package org.chainoptim.desktop.core;

import org.chainoptim.desktop.MainApplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


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
        Parent root = FXMLLoader.load(MainApplication.class.getResource("/org/chainoptim/desktop/core/user/view/Login.fxml"));
        primaryStage.setScene(new Scene(root, 400, 400));
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
