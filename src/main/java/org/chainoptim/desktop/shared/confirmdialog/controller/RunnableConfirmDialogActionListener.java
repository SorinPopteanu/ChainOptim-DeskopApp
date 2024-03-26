package org.chainoptim.desktop.shared.confirmdialog.controller;

import java.util.function.Consumer;

public class RunnableConfirmDialogActionListener<T> implements GenericConfirmDialogActionListener<T> {

    private final Consumer<T> onConfirm;
    private final Runnable onCancel;

    public RunnableConfirmDialogActionListener(Consumer<T> onConfirm, Runnable onCancel) {
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void onConfirmAction(T data) {
        if (onConfirm != null) {
            onConfirm.accept(data);
        }
    }

    @Override
    public void onCancelAction() {
        if (onCancel != null) {
            onCancel.run();
        }
    }
}
