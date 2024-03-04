package org.chainoptim.desktop.shared.util.resourceloader;

import javafx.scene.Node;
import javafx.util.Callback;

public interface FXMLLoaderService {
    Node loadView(String viewPath, Callback<Class<?>, Object> controllerFactory);
}
