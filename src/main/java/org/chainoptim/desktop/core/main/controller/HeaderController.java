package org.chainoptim.desktop.core.main.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HeaderController {

    private final SearchParams searchParams;

    private final NavigationService navigationService;

    @FXML
    private Label title;
    @FXML
    private TextField searchBar;
    @FXML
    private ComboBox sortOptions;
    @FXML
    private Button orderingButton;
    @FXML
    private Button createNewItemButton;
    @FXML
    private Button searchButton;

    private Map<String,String> sortOptionsMap;

    private final Image sortUpIcon = new Image(getClass().getResourceAsStream("/img/sort-up.png"));
    private final Image sortDownIcon = new Image(getClass().getResourceAsStream("/img/sort-down.png"));
    private final ImageView sortUpImageView = new ImageView(sortUpIcon);
    private final ImageView sortDownImageView = new ImageView(sortDownIcon);

    @Inject
    public HeaderController(
            SearchParams searchParams,
            NavigationService navigationService
    ) {
        this.searchParams = searchParams;
        this.navigationService = navigationService;
    }

    public void initializeHeader(String titleText, String titleIconPath, Map<String, String> sortOptionsMap, String createNewItemButtonText) {
        this.sortOptionsMap = sortOptionsMap;
        setSearchButton();
        setTitle(titleText, titleIconPath);
        setOrderingButton();
        setSortOptions(new ArrayList<>(sortOptionsMap.values()));
        setCreateNewItemButton(createNewItemButtonText);
    }

    private void setSearchButton() {
        Image searchIcon = new Image(getClass().getResourceAsStream("/img/search.png"));
        ImageView searchIconView = new ImageView(searchIcon);
        searchIconView.setFitWidth(17);
        searchIconView.setFitHeight(17);
//        ColorAdjust colorAdjust = new ColorAdjust();
//        colorAdjust.setBrightness(1);
//        searchIconView.setEffect(colorAdjust);
        searchButton.setGraphic(searchIconView);
    }

    private void setSortOptions(List<String> sortOptions) {
        this.sortOptions.getItems().addAll(sortOptions);
    }

    private void setOrderingButton() {
            sortDownImageView.setFitWidth(17);
            sortDownImageView.setFitHeight(17);
            sortUpImageView.setFitWidth(17);
            sortUpImageView.setFitHeight(17);
            orderingButton.setGraphic(sortUpImageView);
    }

    private void setTitle(String titleText, String titleIconPath) {
        title.setText(titleText);
        Image titleIcon = new Image(getClass().getResourceAsStream(titleIconPath));
        ImageView titleIconView = new ImageView(titleIcon);
        titleIconView.setFitWidth(21);
        titleIconView.setFitHeight(21);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-1);
        titleIconView.setEffect(colorAdjust);
        title.setGraphic(titleIconView);
        title.setContentDisplay(ContentDisplay.LEFT);

    }

    private void setCreateNewItemButton(String text) {
        createNewItemButton.setText("Create New " + text);
        Image plusIcon = new Image(getClass().getResourceAsStream("/img/plus.png"));
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
        System.out.println(searchParams.getSearchQuery());
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
        String selectedFilter = sortOptions.getValue().toString();
        String backendFilter = sortOptionsMap.entrySet().stream()
            .filter(entry -> Objects.equals(entry.getValue(), selectedFilter))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(selectedFilter);
        searchParams.setSortOption(backendFilter);
        System.out.println(searchParams.getSortOption());
    }

    @FXML
    private void handleCreateNewItem() {
        navigationService.switchView("Create-Product");
    }

}
