package org.chainoptim.desktop.core.user.controller;

import org.chainoptim.desktop.core.user.dto.UserSearchResultDTO;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;
import java.util.*;

public class PublicUsersSearchAndSelectionController implements Initializable {

    private final UserService userService;

    // State
    private final SimpleIntegerProperty currentPage = new SimpleIntegerProperty(1);
    private int totalCount = 0;
    @Getter
    private List<UserSearchResultDTO> selectedUsers = new ArrayList<>();

    // Constants
    private static final int PAGE_SIZE = 1;

    @FXML
    private VBox userResultsVBox;
    @FXML
    private TextField searchInput;
    @FXML
    private Button searchButton;
    @FXML
    private VBox selectedUsersVBox;

    // Icons
    private Image removeIcon;

    @Inject
    public PublicUsersSearchAndSelectionController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupListeners();
        initializeUI();
        searchForUsers();
    }

    private void setupListeners() {
        currentPage.addListener((observable, oldValue, newValue) -> searchForUsers());
        searchInput.setOnAction(event -> {
            currentPage.set(1);
            searchForUsers();
        });
    }

    private void initializeUI() {
        // Search Icon
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/search.png")));
        ImageView searchIconView = new ImageView(image);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        searchIconView.setEffect(colorAdjust);
        searchButton.setGraphic(searchIconView);
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(event -> {
            currentPage.set(1);
            searchForUsers();
        });

        // Remove icon
        removeIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
    }

    private void searchForUsers() {
        this.userService.searchPublicUsers(searchInput.getText(), currentPage.get(), PAGE_SIZE)
                .thenApply(this::handleSearchResponse)
                .exceptionally(this::handleSearchException);
    }

    private Optional<PaginatedResults<UserSearchResultDTO>> handleSearchResponse(Optional<PaginatedResults<UserSearchResultDTO>> optionalPaginatedResults) {
        Platform.runLater(() -> {
            if (optionalPaginatedResults.isEmpty()) {
                return;
            }
            PaginatedResults<UserSearchResultDTO> paginatedResults = optionalPaginatedResults.get();
            totalCount = (int) paginatedResults.getTotalCount();

            // Render Users List + Next Page Button
            userResultsVBox.getChildren().clear();

            if (paginatedResults.getResults().isEmpty()) {
                Label noResultsLabel = new Label("No results found.");
                noResultsLabel.getStyleClass().add("general-label");
                userResultsVBox.getChildren().add(noResultsLabel);
                return;
            }

            for (UserSearchResultDTO user : paginatedResults.getResults()) {
                Button userButton = new Button(user.getUsername());
                userButton.getStyleClass().add("general-label");
                userButton.setOnAction(event -> selectUser(user));
                userResultsVBox.getChildren().add(userButton);
            }

            Button nextPageButton = new Button("Load More");
            nextPageButton.getStyleClass().add("pseudo-link");
            nextPageButton.setStyle("-fx-font-weight: bold; -fx-padding: 10px 0px;");
            nextPageButton.setOnAction(event -> currentPage.set(currentPage.get() + 1));
            userResultsVBox.getChildren().add(nextPageButton);
            if (isAtTheEndOfResults()) {
                nextPageButton.setDisable(true);
                nextPageButton.setVisible(false);
            }
        });
        return optionalPaginatedResults;
    }

    private Optional<PaginatedResults<UserSearchResultDTO>> handleSearchException(Throwable throwable) {
        System.out.println("Error searching for users: " + throwable.getMessage());
        return Optional.empty();
    }

    private void selectUser(UserSearchResultDTO user) {
        if (selectedUsers.stream().map(UserSearchResultDTO::getId).toList().contains(user.getId())) {
            return;
        }

        HBox selectedUserHBox = new HBox();
        Label selectedUserLabel = new Label(user.getUsername());
        selectedUserLabel.getStyleClass().add("general-label");

        Button removeButton = new Button();
        ImageView removeIconView = new ImageView(removeIcon);
        removeIconView.setFitWidth(12);
        removeIconView.setFitHeight(12);
        removeButton.setGraphic(removeIconView);
        removeButton.getStyleClass().add("cancel-edit-button");
        removeButton.setOnAction(event -> {
            selectedUsersVBox.getChildren().remove(selectedUserHBox);
            selectedUsers.remove(user);
        });

        selectedUserHBox.getChildren().addAll(selectedUserLabel, removeButton);
        selectedUserHBox.setAlignment(Pos.CENTER_LEFT);
        selectedUserHBox.setSpacing(8);
        selectedUsers.add(user);
        selectedUsersVBox.getChildren().add(selectedUserHBox);
    }

    private boolean isAtTheEndOfResults() {
        return currentPage.get() >= Math.ceil((double) totalCount / PAGE_SIZE);
    }
}
