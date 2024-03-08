package org.chainoptim.desktop.shared.util.resourceloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;

public class FXMLLoaderServiceImpl implements FXMLLoaderService {

    public Node loadView(String viewPath, Callback<Class<?>, Object> controllerFactory) {
        System.out.println("Loading view: " + viewPath);
        FXMLLoader loader = setUpLoader(viewPath, controllerFactory);
        try {
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FXMLLoader setUpLoader(String viewPath, Callback<Class<?>, Object> controllerFactory) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
        loader.setControllerFactory(controllerFactory);
        return loader;
    }
}
