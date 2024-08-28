package org.chainoptim.desktop.core.main.abstraction;

public interface ControllerFactory {
    <T> T createController(Class<T> type);
}
