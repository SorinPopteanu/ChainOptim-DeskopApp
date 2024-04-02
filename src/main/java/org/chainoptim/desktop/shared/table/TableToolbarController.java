package org.chainoptim.desktop.shared.table;

import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.shared.search.model.SearchParamsImpl;
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

import java.util.Map;
import java.util.Objects;

public class TableToolbarController {

    private final SearchParamsImpl searchParams;
    private final NavigationService navigationService;

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
    @FXML
    private Button editButton;
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
    private final ImageView refreshIconView = new ImageView(refreshIcon);
    private final Image plusIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
    private final ImageView plusIconView = new ImageView(plusIcon);
    private final Map<String, String> sortOptionsMap = Map.of(
            "createdAt", "Created At",
            "updatedAt", "Updated At"
    );

    @Inject
    public TableToolbarController(
            SearchParamsImpl searchParams,
            NavigationService navigationService
    ) {
        this.searchParams = searchParams;
        this.navigationService = navigationService;
    }

    @FXML
    public void initialize() {
        setSearchButton();
        setOrderingButton();
        setRefreshButton(() -> {});
        setEditButton();
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
        refreshIconView.setFitWidth(14);
        refreshIconView.setFitHeight(14);

        // Apply rotation on click
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), refreshIconView);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        refreshButton.setGraphic(refreshIconView);

        refreshButton.setOnAction(e -> {
            rotateTransition.stop();
            rotateTransition.playFromStart();
            // Run refresh action
            refreshAction.run();
        });
    }

    private void setEditButton() {
        editImageView.setFitWidth(16);
        editImageView.setFitHeight(16);
        editButton.setGraphic(editImageView);
    }

    private void setCreateNewOrderButton() {
        plusIconView.setFitWidth(12);
        plusIconView.setFitHeight(12);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        plusIconView.setEffect(colorAdjust);
        createNewOrderButton.setGraphic(plusIconView);
        createNewOrderButton.setContentDisplay(ContentDisplay.LEFT);
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

    @FXML
    private void handleEditButton() {

    }

    @FXML
    private void handleCreateNewOrderButton() {

    }


}
