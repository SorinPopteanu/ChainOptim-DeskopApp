package org.chainoptim.desktop.shared.confirmdialog.model;


public interface ConfirmDialogDataReceiver<T> {

    void setData(T data, ConfirmDialogInput confirmDialogInput);
}
