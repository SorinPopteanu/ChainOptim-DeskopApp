package org.chainoptim.desktop.shared.util.resourceloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;

import java.io.IOException;

public class FXMLLoaderServiceImpl implements FXMLLoaderService {

    public Node loadView(String viewPath, Callback<Class<?>, Object> controllerFactory) {
        try {
            System.out.println("Loading view: " + viewPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            loader.setControllerFactory(controllerFactory);
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();;
            return null;
        }
    }
}
