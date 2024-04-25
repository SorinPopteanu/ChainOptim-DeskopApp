package org.chainoptim.desktop.core.settings.service;

import org.chainoptim.desktop.core.settings.dto.UpdateUserSettingsDTO;
import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserSettingsService {

    CompletableFuture<Result<UserSettings>> getUserSettings(String userId);

    CompletableFuture<Result<UserSettings>> saveUserSettings(UpdateUserSettingsDTO userSettings);
}
