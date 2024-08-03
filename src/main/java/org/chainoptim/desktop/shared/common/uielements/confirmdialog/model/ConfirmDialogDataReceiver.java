package org.chainoptim.desktop.shared.common.uielements.confirmdialog.model;


public interface ConfirmDialogDataReceiver<T> {

    void setData(T data, ConfirmDialogInput confirmDialogInput);
}
