package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.organization.model.CustomRole;

public interface ConfirmUpdateDialogActionListener {

    void onConfirmCustomRoleUpdate(CustomRole customRole);
    void onCancelCustomRoleUpdate();
}
