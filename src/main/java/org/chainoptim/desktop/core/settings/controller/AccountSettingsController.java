package org.chainoptim.desktop.core.settings.controller;

import org.chainoptim.desktop.core.settings.model.UserSettings;
import org.chainoptim.desktop.shared.util.DataReceiver;

public class AccountSettingsController implements DataReceiver<UserSettings> {

    private UserSettings userSettings;

    @Override
    public void setData(UserSettings data) {
        this.userSettings = data;
    }
}
