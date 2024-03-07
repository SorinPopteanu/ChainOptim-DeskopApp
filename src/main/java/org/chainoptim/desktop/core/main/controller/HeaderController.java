package org.chainoptim.desktop.core.main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class HeaderController {

    @FXML
    private Label title;
    @FXML
    private TextField searchBar;
    @FXML
    private ComboBox filters;
    @FXML
    private Button orderingButton;
    @FXML
    private Button createNewItemButton;

    public void initializeHeader(String titleText, String titleIconPath, List<String> filters, String createNewItemButtonText) {
        setTitle(titleText, titleIconPath);
        setOrderingButton();
        setFilters(filters);
        setCreateNewItemButton(createNewItemButtonText);
    }

    private void setFilters(List<String> filters) {
        this.filters.getItems().addAll(filters);
    }

    private void setOrderingButton() {
            Image sortUpIcon = new Image(getClass().getResourceAsStream("/img/sort-up.png"));
            Image sortDownIcon = new Image(getClass().getResourceAsStream("/img/sort-down.png"));
            ImageView sortUpImageView = new ImageView(sortUpIcon);
            ImageView sortDownImageView = new ImageView(sortDownIcon);
            sortDownImageView.setFitWidth(15);
            sortDownImageView.setFitHeight(15);
            orderingButton.setGraphic(sortDownImageView);
            orderingButton.setOnAction(event -> {
                if (orderingButton.getGraphic().equals(sortDownImageView)) {
                    orderingButton.setGraphic(sortUpImageView);
                } else {
                    orderingButton.setGraphic(sortDownImageView);
                }
            });
    }

    private void setTitle(String titleText, String titleIconPath) {

        title.setText(titleText);
        System.out.println(titleText +" "+ titleIconPath);
        Image titleIcon = new Image(getClass().getResourceAsStream(titleIconPath));
        ImageView titleIconView = new ImageView(titleIcon);
        titleIconView.setFitWidth(23);
        titleIconView.setFitHeight(23);
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
        plusIconView.setFitWidth(15);
        plusIconView.setFitHeight(15);
        createNewItemButton.setGraphic(plusIconView);
        createNewItemButton.setContentDisplay(ContentDisplay.LEFT);
    }

}
