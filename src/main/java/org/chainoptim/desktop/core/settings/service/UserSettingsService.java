package org.chainoptim.desktop.core.settings.service;

import org.chainoptim.desktop.core.settings.model.UserSettings;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserSettingsService {

    CompletableFuture<Optional<UserSettings>> getUserSettings(String userId);
}
