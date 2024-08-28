package org.chainoptim.desktop.shared.common.ui.confirmdialog.model;


public interface ConfirmDialogDataReceiver<T> {

    void setData(T data, ConfirmDialogInput confirmDialogInput);
}
