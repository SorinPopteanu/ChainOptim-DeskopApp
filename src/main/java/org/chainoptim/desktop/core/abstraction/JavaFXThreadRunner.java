package org.chainoptim.desktop.core.abstraction;

import javafx.application.Platform;

public class JavaFXThreadRunner implements ThreadRunner {
    @Override
    public void runLater(Runnable action) {
        Platform.runLater(action);
    }
}
