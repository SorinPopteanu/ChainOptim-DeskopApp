package org.chainoptim.desktop.shared.table;

import org.chainoptim.desktop.shared.search.model.SearchParams;

import com.google.inject.Inject;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

public class TableToolbarController {

    // State
    private final SearchParams searchParams;

    private final Map<String, String> sortOptionsMap = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    // FXMl
    @FXML
    private TextField searchBar;
    @FXML
    private Button searchButton;
    @FXML
    private ComboBox<String> sortOptions;
    @FXML
    private Button orderingButton;
    @FXML
    private Button refreshButton;
    @Getter
    @FXML
    private Button cancelRowSelectionButton;
    @Getter
    @FXML
    private Button deleteSelectedRowsButton;
    @Getter
    @FXML
    private Button editSelectedRowsButton;
    @Getter
    @FXML
    private Button saveChangesButton;
    @Getter
    @FXML
    private Button createNewOrderButton;

    // Icons
    private Image sortUpIcon;
    private Image sortDownIcon;
    private Image editIcon;
    private Image searchIcon;
    private Image refreshIcon;
    private Image cancelIcon;
    private Image deleteIcon;
    private Image saveIcon;
    private Image plusIcon;

    @Inject
    public TableToolbarController(
            SearchParams searchParams
    ) {
        this.searchParams = searchParams;
    }

    public void initialize(Runnable refreshAction) {
        initializeIcons();
        setSearchButton();
        setOrderingButton();
        setRefreshButton(refreshAction);
        setCancelRowSelectionButton();
        setDeleteSelectedRowsButton();
        setEditSelectedRowsButton();
        setSaveChangesButton();
        setCreateNewOrderButton();
    }

    private void initializeIcons() {
        sortUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-up.png")));
        sortDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-down.png")));
        editIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
        searchIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/search.png")));
        refreshIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png")));
        cancelIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
        deleteIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
        saveIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
        plusIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
    }

    private void setSearchButton() {
        ImageView searchImageView = createImageView(searchIcon, 16, 16);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        searchImageView.setEffect(colorAdjust);
        searchButton.setGraphic(searchImageView);
    }

    private void setOrderingButton() {
        ImageView sortUpImageView = createImageView(sortUpIcon, 16, 16);
        orderingButton.setGraphic(sortUpImageView);
    }

    public void setRefreshButton(Runnable refreshAction) {
        ImageView refreshImageView = createImageView(refreshIcon, 14, 14);

        // Apply rotation on click
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), refreshImageView);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        refreshButton.setGraphic(refreshImageView);

        refreshButton.setOnAction(e -> {
            rotateTransition.stop();
            rotateTransition.playFromStart();
            // Run refresh action
            refreshAction.run();
        });
    }

    private void setCancelRowSelectionButton() {
        ImageView cancelImageView = createImageView(cancelIcon, 14, 14);
        cancelRowSelectionButton.setGraphic(cancelImageView);
        cancelRowSelectionButton.setVisible(false);
    }

    private void setDeleteSelectedRowsButton() {
        ImageView deleteImageView = createImageView(deleteIcon, 14, 14);
        deleteSelectedRowsButton.setGraphic(deleteImageView);
        deleteSelectedRowsButton.setVisible(false);
    }

    private void setEditSelectedRowsButton() {
        ImageView editImageView = createImageView(editIcon, 14, 14);
        editSelectedRowsButton.setGraphic(editImageView);
        editSelectedRowsButton.setVisible(false);
    }

    private void setSaveChangesButton() {
        ImageView saveImageView = createImageView(saveIcon, 14, 14);
        saveChangesButton.setGraphic(saveImageView);
        toggleButtonVisibility(saveChangesButton, false);
    }

    private void setCreateNewOrderButton() {
        ImageView plusImageView = createImageView(plusIcon, 14, 14);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        plusImageView.setEffect(colorAdjust);
        createNewOrderButton.setGraphic(plusImageView);
        createNewOrderButton.setContentDisplay(ContentDisplay.LEFT);
    }

    public void toggleButtonVisibilityOnSelection(boolean isSelection) {
        toggleButtonVisibility(cancelRowSelectionButton, isSelection);
        toggleButtonVisibility(deleteSelectedRowsButton, isSelection);
        toggleButtonVisibility(editSelectedRowsButton, isSelection);
    }

    public void toggleButtonVisibilityOnEdit(boolean isEditing) {
        toggleButtonVisibility(saveChangesButton, isEditing);
        toggleButtonVisibility(deleteSelectedRowsButton, !isEditing);
        toggleButtonVisibility(editSelectedRowsButton, !isEditing);
    }

    public void toggleButtonVisibilityOnCancel() {
        toggleButtonVisibility(cancelRowSelectionButton, false);
        toggleButtonVisibility(deleteSelectedRowsButton, false);
        toggleButtonVisibility(editSelectedRowsButton, false);
        toggleButtonVisibility(saveChangesButton, false);
    }

    private void toggleButtonVisibility(Button button, boolean isVisible) {
        button.setVisible(isVisible);
        button.setManaged(isVisible);
    }

    @FXML
    private void handleSearch() {
        searchParams.setSearchQuery(searchBar.getText());
    }

    @FXML
    private void handleOrdering() {
        searchParams.setAscending(!searchParams.getAscending());
        if (Boolean.TRUE.equals(searchParams.getAscending())) {
            ImageView sortUpImageView = createImageView(sortUpIcon, 16, 16);
            orderingButton.setGraphic(sortUpImageView);
        } else {
            ImageView sortDownImageView = createImageView(sortUpIcon, 16, 16);
            orderingButton.setGraphic(sortDownImageView);
        }
    }

    @FXML
    private void handleSortOption() {
        String selectedSortOption = sortOptions.getValue();
        String backendSortOption = sortOptionsMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), selectedSortOption))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(selectedSortOption);
        searchParams.setSortOption(backendSortOption);
    }

    private ImageView createImageView(Image image, int width, int height) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        return imageView;
    }
}
