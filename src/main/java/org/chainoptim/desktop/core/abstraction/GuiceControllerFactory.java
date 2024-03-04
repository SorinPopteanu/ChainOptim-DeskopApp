package org.chainoptim.desktop.core.abstraction;

import org.chainoptim.desktop.MainApplication;
import org.chainoptim.desktop.core.abstraction.ControllerFactory;

public class GuiceControllerFactory implements ControllerFactory {

    @Override
    public <T> T createController(Class<T> type) {
        return MainApplication.injector.getInstance(type);
    }
}
