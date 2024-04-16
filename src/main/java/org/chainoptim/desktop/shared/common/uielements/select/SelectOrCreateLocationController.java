package org.chainoptim.desktop.shared.common.uielements.select;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.features.location.service.LocationService;

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
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField stateField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField zipCodeField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;

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

        loadLocations();
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

    private Optional<List<Location>> handleLocationsResponse(Optional<List<Location>> locationsOptional) {
        Platform.runLater(() -> {
            if (locationsOptional.isEmpty()) {
                return;
            }
            locationComboBox.getItems().setAll(locationsOptional.get());

        });
        return locationsOptional;
    }

    private Optional<List<Location>> handleLocationsException(Throwable ex) {
        return Optional.empty();
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

    public CreateLocationDTO getNewLocationDTO() {
        CreateLocationDTO locationDTO = new CreateLocationDTO();
        locationDTO.setOrganizationId(organizationId);
        locationDTO.setAddress(addressField.getText());
        locationDTO.setCity(cityField.getText());
        locationDTO.setState(stateField.getText());
        locationDTO.setCountry(countryField.getText());
        locationDTO.setZipCode(zipCodeField.getText());

        String latitudeStr = latitudeField.getText().trim();
        if (!latitudeStr.isEmpty()) {
            try {
                double latitude = Double.parseDouble(latitudeStr);
                locationDTO.setLatitude(latitude);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing longitude");
            }
        }

        String longitudeStr = longitudeField.getText().trim();
        if (!longitudeStr.isEmpty()) {
            try {
                double longitude = Double.parseDouble(longitudeStr);
                locationDTO.setLongitude(longitude);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing longitude");
            }
        }
        return locationDTO;
    }

    public boolean isCreatingNewLocation() {
        return createNewRadio.isSelected();
    }
}
