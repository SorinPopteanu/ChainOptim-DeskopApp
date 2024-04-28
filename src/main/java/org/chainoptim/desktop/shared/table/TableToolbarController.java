package org.chainoptim.desktop.shared.table;

import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.shared.common.uielements.info.InfoLabel;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.SearchMode;
import org.chainoptim.desktop.shared.search.filters.FilterBar;
import org.chainoptim.desktop.shared.search.filters.FilterOption;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TableToolbarController {

    // State
    private SearchParams searchParams;
    private Map<String,String> sortOptionsMap;

    // FXML
    @FXML
    private HBox titleContainer;
    @FXML
    private Label title;
    @FXML
    private InfoLabel featureInfoLabel;
    @FXML
    private TextField searchBar;
    @FXML
    private Button searchButton;
    @FXML
    private FilterBar filterBar;
    @FXML
    private ComboBox<String> sortOptions;
    @FXML
    private Button orderingButton;
    @FXML
    private Button refreshButton;

    @FXML @Getter
    private Button cancelRowSelectionButton;
    @FXML @Getter
    private Button deleteSelectedRowsButton;
    @FXML @Getter
    private Button editSelectedRowsButton;
    @FXML @Getter
    private Button saveChangesButton;
    @FXML @Getter
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

    public void initialize(SearchMode searchMode, SearchParams searchParams,
                           String titleText, String titleIconPath, Feature feature,
                           List<FilterOption> filterOptions, Map<String, String> sortOptionsMap,
                           Runnable refreshAction) {
        this.searchParams = searchParams;
        this.sortOptionsMap = sortOptionsMap;
        initializeIcons();
        setTitle(titleText, titleIconPath);
        setInfoLabel(feature);
        if (searchMode == SearchMode.SECONDARY) {
            titleContainer.setVisible(false);
            titleContainer.setManaged(false);
        }
        setSearchButton();
        filterBar.initializeFilterBar(filterOptions, searchParams);
        setOrderingButton();
        setSortOptions(new ArrayList<>(sortOptionsMap.values()));
        setRefreshButton(refreshAction);
        setCancelRowSelectionButton();
        setDeleteSelectedRowsButton();
        setEditSelectedRowsButton();
        setSaveChangesButton();
        setCreateNewOrderButton();

        toggleButtonVisibilityOnCancel();
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

    private void setTitle(String titleText, String titleIconPath) {
        title.setText(titleText);
        Image titleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(titleIconPath)));
        ImageView titleIconView = new ImageView(titleIcon);
        titleIconView.setFitWidth(16);
        titleIconView.setFitHeight(16);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.84);
        titleIconView.setEffect(colorAdjust);
        title.setGraphic(titleIconView);
        title.setContentDisplay(ContentDisplay.LEFT);
    }

    private void setInfoLabel(Feature feature) {
        featureInfoLabel.setFeatureAndLevel(feature,
                TenantSettingsContext.getCurrentUserSettings().getGeneralSettings().getInfoLevel());
    }

    private void setSearchButton() {
        ImageView searchImageView = createImageView(searchIcon, 16, 16);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        searchImageView.setEffect(colorAdjust);
        searchButton.setGraphic(searchImageView);
    }

    private void setSortOptions(List<String> sortOptions) {
        this.sortOptions.getItems().addAll(sortOptions);
    }

    private void setOrderingButton() {
        ImageView sortUpImageView = createImageView(
                Boolean.TRUE.equals(searchParams.getAscending()) ? sortUpIcon : sortDownIcon, 16, 16);
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

    public void toggleButtonVisibilityOnCreate(boolean isNewOrderMode) {
        toggleButtonVisibility(createNewOrderButton, isNewOrderMode);
        toggleButtonVisibility(saveChangesButton, isNewOrderMode);
        toggleButtonVisibility(cancelRowSelectionButton, isNewOrderMode);
        toggleButtonVisibility(deleteSelectedRowsButton, !isNewOrderMode);
        toggleButtonVisibility(editSelectedRowsButton, !isNewOrderMode);
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
            ImageView sortDownImageView = createImageView(sortDownIcon, 16, 16);
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
