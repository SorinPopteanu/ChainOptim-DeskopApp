package org.chainoptim.desktop.shared.common.uielements.select;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.features.location.service.LocationService;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.util.List;
import java.util.Optional;

public class SelectOrCreateLocationController {

    private final LocationService locationService;

    @FXML
    private RadioButton selectExistingRadio;
    @FXML
    private RadioButton createNewRadio;
    @FXML
    private ComboBox<Location> locationComboBox;
    @FXML
    private VBox createLocationForm;
    @FXML
    private FormField<String> addressField;
    @FXML
    private FormField<String> cityField;
    @FXML
    private FormField<String> stateField;
    @FXML
    private FormField<String> countryField;
    @FXML
    private FormField<String> zipCodeField;
    @FXML
    private FormField<Double> latitudeField;
    @FXML
    private FormField<Double> longitudeField;

    private final ToggleGroup locationToggleGroup = new ToggleGroup();

    private Integer organizationId;

    @Inject
    public SelectOrCreateLocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    public void initialize() {
        selectExistingRadio.setToggleGroup(locationToggleGroup);
        createNewRadio.setToggleGroup(locationToggleGroup);

        toggleVisibilityBasedOnSelection();

        locationToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            toggleVisibilityBasedOnSelection();
        });

        initializeFormFields();

        loadLocations();
    }

    private void initializeFormFields() {
        addressField.initialize(String::new, "Address", false, null, "Your input is not valid.");
        cityField.initialize(String::new, "City", false, null, "Your input is not valid.");
        stateField.initialize(String::new, "State", false, null, "Your input is not valid.");
        countryField.initialize(String::new, "Country", false, null, "Your input is not valid.");
        zipCodeField.initialize(String::new, "Zip Code", false, null, "Your input is not valid.");
        latitudeField.initialize(Double::parseDouble, "Latitude", false, null, "Your input is not a number.");
        longitudeField.initialize(Double::parseDouble, "Longitude", false, null, "Your input is not a number.");
    }

    private void loadLocations() {
        locationComboBox.setCellFactory(lv -> new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFormattedLocation());
            }
        });

        locationComboBox.setButtonCell(new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFormattedLocation());
                }
            }
        });

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        organizationId = currentUser.getOrganization().getId();

        locationService.getLocationsByOrganizationId(organizationId)
                .thenApply(this::handleLocationsResponse)
                .exceptionally(this::handleLocationsException);
    }

    private Result<List<Location>> handleLocationsResponse(Result<List<Location>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            locationComboBox.getItems().setAll(result.getData());

        });
        return result;
    }

    private Result<List<Location>> handleLocationsException(Throwable ex) {
        return new Result<>();
    }

    private void toggleVisibilityBasedOnSelection() {
        boolean isSelectExistingSelected = selectExistingRadio.isSelected();
        locationComboBox.setVisible(isSelectExistingSelected);
        locationComboBox.setManaged(isSelectExistingSelected);
        createLocationForm.setVisible(!isSelectExistingSelected);
        createLocationForm.setManaged(!isSelectExistingSelected);
    }

    public Location getSelectedLocation() {
        return locationComboBox.getSelectionModel().getSelectedItem();
    }

    public void setSelectedLocation(Location location) {
        locationComboBox.getSelectionModel().select(location);

    }

    public CreateLocationDTO getNewLocationDTO() throws ValidationException {
        CreateLocationDTO locationDTO = new CreateLocationDTO();
        locationDTO.setOrganizationId(organizationId);
        locationDTO.setAddress(addressField.handleSubmit());
        locationDTO.setCity(cityField.handleSubmit());
        locationDTO.setState(stateField.handleSubmit());
        locationDTO.setCountry(countryField.handleSubmit());
        locationDTO.setZipCode(zipCodeField.handleSubmit());
        locationDTO.setLatitude(latitudeField.handleSubmit());
        locationDTO.setLongitude(longitudeField.handleSubmit());

        return locationDTO;
    }

    public boolean isCreatingNewLocation() {
        return createNewRadio.isSelected();
    }
}
