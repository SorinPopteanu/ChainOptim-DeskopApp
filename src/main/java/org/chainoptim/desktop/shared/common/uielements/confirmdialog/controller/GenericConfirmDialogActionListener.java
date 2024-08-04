package org.chainoptim.desktop.shared.common.uielements.confirmdialog.controller;

/**
 * A generic interface for listening to confirm/cancel actions on a dialog.
 */
public interface GenericConfirmDialogActionListener<T> {

    void onConfirmAction(T data);
    void onCancelAction();
}
