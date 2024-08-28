package org.chainoptim.desktop.shared.common.ui.select;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.chainoptim.desktop.shared.util.TimeUtil;

import java.util.Objects;

import static java.lang.Float.parseFloat;

public class SelectDurationController {

    @FXML
    private TextField timeInput;
    @FXML
    private ComboBox<String> timePeriodSelect;

    @FXML
    private void initialize() {
        timePeriodSelect.getSelectionModel().select("Hours");
    }

    public Float getTimeSeconds() {
        if (timeInput != null && !Objects.equals(timeInput.getText(), "") && timePeriodSelect.getValue() != null) {
            float inputDuration = parseFloat(timeInput.getText());
            return TimeUtil.getSeconds(inputDuration, timePeriodSelect.getValue());
        } else {
            return -1.0f; // Marker for invalid input
        }
    }

    public void setTime(Float timeSeconds) {
        if (timeSeconds != null && timeSeconds > 0) {
            String timePeriod = "Hours";
            timePeriodSelect.setValue(timePeriod);
            float durationHours = TimeUtil.getDuration(timeSeconds, timePeriod);
            if (durationHours != -1.0f) {
                timeInput.setText(String.valueOf(durationHours));
            } else {
                timeInput.setText("");
            }
        }
    }
}
