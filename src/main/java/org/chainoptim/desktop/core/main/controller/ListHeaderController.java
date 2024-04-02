package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListHeaderController {

    private final NavigationService navigationService;
    private SearchParams searchParams;
    private String createNewItem;

    @FXML
    private Label title;
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
    private Button createNewItemButton;

    private Map<String,String> sortOptionsMap;

    private final Image sortUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-up.png")));
    private final Image sortDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sort-down.png")));
    private final ImageView sortUpImageView = new ImageView(sortUpIcon);
    private final ImageView sortDownImageView = new ImageView(sortDownIcon);

    @Inject
    public ListHeaderController(
            NavigationService navigationService
    ) {
        this.navigationService = navigationService;
    }

    public void initializeHeader(SearchParams searchParams,
                                 String titleText, String titleIconPath,
                                 Map<String, String> sortOptionsMap,
                                 Runnable refreshAction,
                                 String createNewItemButtonText, String createNewItem) {
        this.searchParams = searchParams;
        this.sortOptionsMap = sortOptionsMap;
        setTitle(titleText, titleIconPath);
        setSearchButton();
        setOrderingButton();
        setSortOptions(new ArrayList<>(sortOptionsMap.values()));
        setRefreshButton(refreshAction);
        setCreateNewItemButton(createNewItemButtonText);
        setNewItemKey(createNewItem);
    }

    private void setTitle(String titleText, String titleIconPath) {
        title.setText(titleText);
        Image titleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(titleIconPath)));
        ImageView titleIconView = new ImageView(titleIcon);
        titleIconView.setFitWidth(18);
        titleIconView.setFitHeight(18);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-1);
        titleIconView.setEffect(colorAdjust);
        title.setGraphic(titleIconView);
        title.setContentDisplay(ContentDisplay.LEFT);
    }

    private void setSearchButton() {
        Image searchIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/search.png")));
        ImageView searchIconView = new ImageView(searchIcon);
        searchIconView.setFitWidth(16);
        searchIconView.setFitHeight(16);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        searchIconView.setEffect(colorAdjust);
        searchButton.setGraphic(searchIconView);
    }

    private void setSortOptions(List<String> sortOptions) {
        this.sortOptions.getItems().addAll(sortOptions);
    }

    private void setOrderingButton() {
            sortDownImageView.setFitWidth(16);
            sortDownImageView.setFitHeight(16);
            sortUpImageView.setFitWidth(16);
            sortUpImageView.setFitHeight(16);
            orderingButton.setGraphic(sortUpImageView);
    }

    public void setRefreshButton(Runnable refreshAction) {
        Image refreshIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png")));
        ImageView refreshIconView = new ImageView(refreshIcon);
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

    private void setCreateNewItemButton(String text) {
        createNewItemButton.setText("Create New " + text);
        Image plusIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
        ImageView plusIconView = new ImageView(plusIcon);
        plusIconView.setFitWidth(12);
        plusIconView.setFitHeight(12);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        plusIconView.setEffect(colorAdjust);
        createNewItemButton.setGraphic(plusIconView);
        createNewItemButton.setContentDisplay(ContentDisplay.LEFT);
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

    private void setNewItemKey(String createNewItem) {
        this.createNewItem = createNewItem;
    }

    @FXML
    private void handleCreateNewItem() {
        navigationService.switchView(createNewItem, true);
    }

}
