package org.chainoptim.desktop.shared.common.uielements.select;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;
import org.chainoptim.desktop.features.product.service.UnitOfMeasurementService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;

public class SelectOrCreateUnitOfMeasurementController {

    private final UnitOfMeasurementService unitOfMeasurementService;

    @FXML
    private RadioButton selectExistingRadio;
    @FXML
    private RadioButton createNewRadio;
    @FXML
    private ComboBox<UnitOfMeasurement> unitComboBox;
    @FXML
    private VBox createUnitForm;
    @FXML
    private FormField<String> unitNameField;
    @FXML
    private FormField<String> unitTypeField;

    private final ToggleGroup unitToggleGroup = new ToggleGroup();

    private Integer organizationId;

    @Inject
    public SelectOrCreateUnitOfMeasurementController(UnitOfMeasurementService unitOfMeasurementService) {
        this.unitOfMeasurementService = unitOfMeasurementService;
    }

    public void initialize() {
        selectExistingRadio.setToggleGroup(unitToggleGroup);
        createNewRadio.setToggleGroup(unitToggleGroup);

        toggleVisibilityBasedOnSelection();

        unitToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            toggleVisibilityBasedOnSelection();
        });

        initializeFormFields();

        loadUnitsOfMeasurement();
    }

    private void loadUnitsOfMeasurement() {
        unitComboBox.setCellFactory(lv -> new ListCell<UnitOfMeasurement>() {
            @Override
            protected void updateItem(UnitOfMeasurement item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        unitComboBox.setButtonCell(new ListCell<UnitOfMeasurement>() {
            @Override
            protected void updateItem(UnitOfMeasurement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        organizationId = currentUser.getOrganization().getId();

        unitOfMeasurementService.getUnitsOfMeasurementByOrganizationId(organizationId)
                .thenApply(this::handleUnitsResponse)
                .exceptionally(this::handleUnitsException);
    }

    private Result<List<UnitOfMeasurement>> handleUnitsResponse(Result<List<UnitOfMeasurement>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            unitComboBox.getItems().setAll(result.getData());

        });
        return result;
    }

    private Result<List<UnitOfMeasurement>> handleUnitsException(Throwable ex) {
        return new Result<>();
    }

    private void initializeFormFields() {
        unitNameField.initialize(String::new, "Name", true, null, "Your input is not valid.");
        unitTypeField.initialize(String::new, "Type", true, null, "Your input is not valid.");
    }

    private void toggleVisibilityBasedOnSelection() {
        boolean isSelectExistingSelected = selectExistingRadio.isSelected();
        unitComboBox.setVisible(isSelectExistingSelected);
        unitComboBox.setManaged(isSelectExistingSelected);
        createUnitForm.setVisible(!isSelectExistingSelected);
        createUnitForm.setManaged(!isSelectExistingSelected);
    }

    public UnitOfMeasurement getSelectedUnit() {
        return unitComboBox.getSelectionModel().getSelectedItem();
    }

    public CreateUnitOfMeasurementDTO getNewUnitDTO() throws ValidationException {
        CreateUnitOfMeasurementDTO unitDTO = new CreateUnitOfMeasurementDTO();
        unitDTO.setOrganizationId(organizationId);

        unitDTO.setName(unitNameField.handleSubmit());
        unitDTO.setUnitType(unitTypeField.handleSubmit());

        return unitDTO;
    }

    public boolean isCreatingNewUnit() {
        return createNewRadio.isSelected();
    }
}
