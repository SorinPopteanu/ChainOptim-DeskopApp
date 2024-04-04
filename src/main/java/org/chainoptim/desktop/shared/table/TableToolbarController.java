package org.chainoptim.desktop.shared.table;

import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.features.supplier.controller.SupplierOrdersController;
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

    private final SearchParams searchParams;
    private final NavigationService navigationService;
    private final SupplierOrdersController supplierOrdersController;

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

    private final Image sortUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-up.png")));
    private final ImageView sortUpImageView = new ImageView(sortUpIcon);
    private final Image sortDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-down.png")));
    private final ImageView sortDownImageView = new ImageView(sortDownIcon);
    private final Image editIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pen-to-square-solid.png")));
    private final ImageView editImageView = new ImageView(editIcon);
    private final Image searchIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/search.png")));
    private final ImageView searchImageView = new ImageView(searchIcon);
    private final Image refreshIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png")));
    private final ImageView refreshImageView = new ImageView(refreshIcon);
    private final Image cancelIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
    private final ImageView cancelImageView =new ImageView (cancelIcon);
    private final Image deleteIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
    private final ImageView deleteImageView = new ImageView(deleteIcon);
    private final Image saveIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
    private final ImageView saveImageView = new ImageView(saveIcon);
    private final Image plusIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
    private final ImageView plusImageView = new ImageView(plusIcon);
    private final Map<String, String> sortOptionsMap = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    @Inject
    public TableToolbarController(
            SearchParams searchParams,
            NavigationService navigationService,
            SupplierOrdersController supplierOrdersController
    ) {
        this.searchParams = searchParams;
        this.navigationService = navigationService;
        this.supplierOrdersController = supplierOrdersController;
    }

    @FXML
    public void initialize() {
        setSearchButton();
        setOrderingButton();
        setRefreshButton(() -> {});
        setCancelRowSelectionButton();
        setDeleteSelectedRowsButton();
        setEditSelectedRowsButton();
        setSaveChangesButton();
        setCreateNewOrderButton();
    }

    private void setSearchButton() {
        searchImageView.setFitWidth(16);
        searchImageView.setFitHeight(16);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        searchImageView.setEffect(colorAdjust);
        searchButton.setGraphic(searchImageView);
    }

    private void setOrderingButton() {
        sortDownImageView.setFitWidth(16);
        sortDownImageView.setFitHeight(16);
        sortUpImageView.setFitWidth(16);
        sortUpImageView.setFitHeight(16);
        orderingButton.setGraphic(sortUpImageView);
    }

    public void setRefreshButton(Runnable refreshAction) {
        refreshImageView.setFitWidth(14);
        refreshImageView.setFitHeight(14);

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
        cancelImageView.setFitWidth(16);
        cancelImageView.setFitHeight(16);
        cancelRowSelectionButton.setGraphic(cancelImageView);
        cancelRowSelectionButton.setVisible(false);
    }

    private void setDeleteSelectedRowsButton() {
        deleteImageView.setFitWidth(16);
        deleteImageView.setFitHeight(16);
        deleteSelectedRowsButton.setGraphic(deleteImageView);
        deleteSelectedRowsButton.setVisible(false);
    }

    private void setEditSelectedRowsButton() {
        editImageView.setFitWidth(16);
        editImageView.setFitHeight(16);
        editSelectedRowsButton.setGraphic(editImageView);
        editSelectedRowsButton.setVisible(false);
    }

    private void setSaveChangesButton() {
        saveImageView.setFitWidth(16);
        saveImageView.setFitHeight(16);
        saveChangesButton.setGraphic(saveImageView);
        saveChangesButton.setVisible(false);
    }

    private void setCreateNewOrderButton() {
        plusImageView.setFitWidth(12);
        plusImageView.setFitHeight(12);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        plusImageView.setEffect(colorAdjust);
        createNewOrderButton.setGraphic(plusImageView);
        createNewOrderButton.setContentDisplay(ContentDisplay.LEFT);
    }

    public void setButtonsAvailability(boolean value) {
        cancelRowSelectionButton.setVisible(value);
        deleteSelectedRowsButton.setVisible(value);
        editSelectedRowsButton.setVisible(value);
    }

    @FXML
    private void handleSearch() {
        searchParams.setSearchQuery(searchBar.getText());
    }

    @FXML
    private void handleOrdering() {
        searchParams.setAscending(!searchParams.getAscending());
        if (orderingButton.getGraphic().equals(sortDownImageView)) {
            orderingButton.setGraphic(sortUpImageView);
        } else {
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
}
