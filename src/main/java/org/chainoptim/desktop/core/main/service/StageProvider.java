package org.chainoptim.desktop.core.main.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.stage.Stage;

public class StageProvider implements Provider<Stage> {

    private final Stage stage;

    @Inject
    public StageProvider(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage get() {
        return stage;
    }
}
