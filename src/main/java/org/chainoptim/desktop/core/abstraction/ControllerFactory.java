package org.chainoptim.desktop.core.abstraction;

public interface ControllerFactory {
    <T> T createController(Class<T> type);
}
