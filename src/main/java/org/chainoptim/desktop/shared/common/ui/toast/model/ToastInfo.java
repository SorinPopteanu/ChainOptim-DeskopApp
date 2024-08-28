package org.chainoptim.desktop.shared.common.ui.toast.model;

import org.chainoptim.desktop.shared.enums.OperationOutcome;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToastInfo {

    private String title;
    private String message;
    private OperationOutcome operationOutcome;
}
