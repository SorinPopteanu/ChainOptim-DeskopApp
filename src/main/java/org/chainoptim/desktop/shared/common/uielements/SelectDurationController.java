package org.chainoptim.desktop.shared.common.uielements;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.Getter;
import org.chainoptim.desktop.shared.util.TimeUtil;

import java.util.Objects;

import static java.lang.Float.parseFloat;

public class SelectDurationController {

    @FXML
    private TextField timeInput;
    @FXML
    private ComboBox<String> timePeriodSelect;

    public Float getTimeSeconds() {
        if (timeInput != null && !Objects.equals(timeInput.getText(), "") && timePeriodSelect.getValue() != null) {
            float inputDuration = parseFloat(timeInput.getText());
            float durationSeconds = TimeUtil.getSeconds(inputDuration, timePeriodSelect.getValue());
            return durationSeconds;
        } else {
            return -1.0f; // Marker for invalid input
        }
    }
}
