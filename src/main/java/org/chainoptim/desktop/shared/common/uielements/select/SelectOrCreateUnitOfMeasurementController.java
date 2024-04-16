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
    private TextField nameField;
    @FXML
    private TextField unitTypeField;

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

    private Optional<List<UnitOfMeasurement>> handleUnitsResponse(Optional<List<UnitOfMeasurement>> unitsOptional) {
        Platform.runLater(() -> {
            if (unitsOptional.isEmpty()) {
                return;
            }
            unitComboBox.getItems().setAll(unitsOptional.get());

        });
        return unitsOptional;
    }

    private Optional<List<UnitOfMeasurement>> handleUnitsException(Throwable ex) {
        return Optional.empty();
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

    public CreateUnitOfMeasurementDTO getNewUnitDTO() {
        CreateUnitOfMeasurementDTO unitDTO = new CreateUnitOfMeasurementDTO();
        unitDTO.setOrganizationId(organizationId);
        unitDTO.setName(nameField.getText());
        unitDTO.setUnitType(unitTypeField.getText());
        return unitDTO;
    }

    public boolean isCreatingNewUnit() {
        return createNewRadio.isSelected();
    }
}
