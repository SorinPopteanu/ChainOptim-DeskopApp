package org.chainoptim.desktop.core.main.abstraction;

import org.chainoptim.desktop.MainApplication;

public class GuiceControllerFactory implements ControllerFactory {

    @Override
    public <T> T createController(Class<T> type) {
        return MainApplication.injector.getInstance(type);
    }
}
