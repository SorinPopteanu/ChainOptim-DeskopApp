package org.chainoptim.desktop.shared.common.ui.forms;

import org.chainoptim.desktop.shared.table.util.StringConverter;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FormField<T> extends VBox {

    // Initial state
    private StringConverter<T> stringConverter;
    private String formLabel;
    private boolean isMandatory;
    private String errorMessage;

    // Current state
    private String currentValue;
    private boolean hasTriedSubmitting = false;

    // UI
    private TextField textField;
    private Label errorLabel;
    private Label mandatoryErrorLabel;

    public void initialize(StringConverter<T> stringConverter, String formLabel, boolean isMandatory, T initialValue, String errorMessage) {
        this.stringConverter = stringConverter;
        this.formLabel = formLabel;
        this.isMandatory = isMandatory;
        this.errorMessage = errorMessage;

        this.currentValue = initialValue != null ? initialValue.toString() : "";

        initializeUI();
    }

    public void setInitialValue(T initialValue) {
        this.currentValue = initialValue != null ? initialValue.toString() : "";
        textField.setText(currentValue);
    }

    private void initializeUI() {
        // Label
        TextFlow label = new TextFlow();
        Text labelText = new Text(formLabel);
        label.getChildren().add(labelText);
        if (isMandatory) {
            Text mandatoryText = new Text(" *");
            mandatoryText.setStyle("-fx-fill: #B22222;");
            label.getChildren().add(mandatoryText);
        }
        label.getStyleClass().add("form-label");
        this.getChildren().add(label);

        textField = new TextField(currentValue);
        textField.getStyleClass().setAll("custom-text-field");
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentValue = newValue;
            if (hasTriedSubmitting) {
                checkValidity();
            }
        });
        this.getChildren().add(textField);

        VBox errorVBox = new VBox(4);
        errorVBox.setStyle("-fx-padding: 4px 0px 0px 0px;");
        errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("form-error-message");
        toggleNodeVisibility(errorLabel, false);
        errorVBox.getChildren().add(errorLabel);

        mandatoryErrorLabel = new Label("This field is mandatory.");
        mandatoryErrorLabel.getStyleClass().add("form-error-message");
        toggleNodeVisibility(mandatoryErrorLabel, false);
        errorVBox.getChildren().add(mandatoryErrorLabel);

        this.getChildren().add(errorVBox);
    }

    private void checkValidity() {
        String input = textField.getText();
        if (input.isEmpty()) {
            if (isMandatory) {
                toggleNodeVisibility(mandatoryErrorLabel, true);
            }
            return; // Skip parsing for non-mandatory fields
        }
        try {
            stringConverter.convert(input);
            toggleNodeVisibility(errorLabel, false);
            toggleNodeVisibility(mandatoryErrorLabel, false);
        } catch (Exception e) {
            toggleNodeVisibility(errorLabel, true);
        }
    }

    public T handleSubmit() throws ValidationException {
        hasTriedSubmitting = true;

        String input = textField.getText();
        if (input.isEmpty()) {
            if (isMandatory) {
                toggleNodeVisibility(mandatoryErrorLabel, true);
                throw new ValidationException("Mandatory field is empty.");
            }
            return null; // Skip parsing for non-mandatory fields
        }

        try {
            T value = stringConverter.convert(input);
            toggleNodeVisibility(errorLabel, false);
            toggleNodeVisibility(mandatoryErrorLabel, false);
            return value;
        } catch (Exception e) {
            toggleNodeVisibility(errorLabel, true);
            throw new ValidationException("Mandatory field is empty.");
        }
    }

    private void toggleNodeVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
