package org.chainoptim.desktop.shared.confirmdialog.controller;

public interface GenericConfirmDialogActionListener<T> {

    void onConfirmAction(T data);
    void onCancelAction();
}
