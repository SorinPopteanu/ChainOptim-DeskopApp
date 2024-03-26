package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.core.user.service.UserService;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class OrganizationOverviewController implements DataReceiver<OrganizationViewData> {

    // Services
    private final UserService userService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    // Listeners
    private RunnableConfirmDialogActionListener<Pair<String, Integer>> confirmDialogUpdateListener; // Pair (userId, customRoleId)

    // State
    private OrganizationViewData organizationViewData;
    private boolean isInitialRender = true;
    private int editedUserRowId = -1; // Marker for no edit
    private boolean isDeleteMode = false;
    private final FallbackManager fallbackManager;

    // FXML
    @FXML
    private Label tabTitle;
    @FXML
    private Button removeMemberButton;
    @FXML
    private Button addNewMemberButton;
    @FXML
    private GridPane membersGridPane;
    Popup assignRolePopup;

    // Constants
    private static final String[] headers = {"Username", "Role", "Custom Role", "Joined At", "Email"};
    private static final int ASSIGN_ROLE_COLUMN_INDEX = 2;

    // Icons
    private Image plusImage;
    private Image trashImage;

    @Inject
    public OrganizationOverviewController(UserService userService,
                                          FXMLLoaderService fxmlLoaderService,
                                          ControllerFactory controllerFactory,
                                          FallbackManager fallbackManager) {
        this.userService = userService;
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(OrganizationViewData data) {
        this.organizationViewData = data;

        // Render only once when organization is received, as custom roles may be received later from separate thread
        if (organizationViewData.getOrganization() != null && isInitialRender) {
            initializeOrganizationUI();
            isInitialRender = false;
        }
    }

    private void initializeOrganizationUI() {
        if (organizationViewData.getOrganization().getUsers() == null) {
            fallbackManager.setErrorMessage("Failed to load organization members");
            return;
        }

        initializeIcons();
        initializeTitleContainer();
        setupListeners();

        // Render members grid
        renderMembersGrid();
    }

    private void initializeIcons() {
        plusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/plus.png")));
        trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/trash-solid.png")));
    }

    private void initializeTitleContainer() {
        tabTitle.setText("Members (" + organizationViewData.getOrganization().getUsers().size() + ")");
        styleDeleteRoleButton(removeMemberButton);
        removeMemberButton.setOnAction(event -> toggleDeleteMode());
        styleAddNewRoleButton(addNewMemberButton);
        addNewMemberButton.setOnAction(event -> {});
    }

    private void setupListeners() {
        Consumer<Pair<String, Integer>> onConfirmUpdate = this::assignRole;
        Runnable onCancelUpdate = this::cancelAssignRole;

        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);
    }

    private void renderMembersGrid() {
        membersGridPane.getChildren().clear();

        // Headers
        for (int i = 0; i < headers.length; i++) {
            Label label = new Label(headers[i]);
            label.getStyleClass().add("column-header");
            membersGridPane.add(label, i, 0);
            GridPane.setHalignment(label, HPos.CENTER);
        }

        // Rows
        int row = 1;
        for (User user : organizationViewData.getOrganization().getUsers()) {
            for (int i = 0; i < headers.length; i++) {
                Node node = getDisplayedPropertyByHeader(user, row, i);
                if (i > 0) applyStandardMargin(node);
                membersGridPane.add(node, i, row);
                GridPane.setHalignment(node, HPos.CENTER);
            }
            row++;
        }
    }

    private Node getDisplayedPropertyByHeader(User user, int rowIndex, int headerIndex) {
        String header = headers[headerIndex];
        if (header.equals("Custom Role")) {
            if (user.getCustomRole() != null) {
                return new Label(user.getCustomRole().getName());
            }
            Button button = new Button("Assign");
            button.getStyleClass().add("pseudo-link");
            button.setOnAction(event -> {
                System.out.println("Assigning role to user: " + user.getUsername() + " rowId: " + rowIndex + " headerIndex: " + headerIndex);
                editedUserRowId = rowIndex;
                loadAssignRoleDialog(user, event);
            });
            return button;
        }
        return switch (header) {
            case "Username" -> new Label(user.getUsername());
            case "Role" -> new Label(user.getRole().toString());
            case "Joined At" -> new Label(user.getCreatedAt().toString());
            case "Email" -> new Label(user.getEmail());
            default -> null;
        };
    }

    private void loadAssignRoleDialog(User user, ActionEvent event) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/OrganizationAssignRoleView.fxml",
                controllerFactory::createController
        );
        try {
            Node content = loader.load();
            OrganizationAssignRoleController controller = loader.getController();
            controller.setData(new Pair<>(user, organizationViewData.getCustomRoles()));
            controller.setActionListener(confirmDialogUpdateListener);

            assignRolePopup = new Popup();
            assignRolePopup.getContent().add(content);
            assignRolePopup.setAutoHide(true);

            Button sourceButton = (Button) event.getSource(); // Safe cast, source is a Button
            double x = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMinX() - 120; // Center the popup
            double y = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxY();
            assignRolePopup.show(sourceButton, x, y);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void assignRole(Pair<String, Integer> userIdCustomRoleId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        userService.assignCustomRoleToUser(userIdCustomRoleId.getKey(), userIdCustomRoleId.getValue())
                .thenApply(this::handleRoleResponse)
                .exceptionally(this::handleRoleException);
    }

    private Optional<User> handleRoleResponse(Optional<User> userOptional) {
        Platform.runLater(() -> {
            if (userOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to assign role");
                return;
            }
            fallbackManager.setLoading(false);

            // Turn Assign button into Custom Role label
            Node assignNode = membersGridPane.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == editedUserRowId && GridPane.getColumnIndex(node) == ASSIGN_ROLE_COLUMN_INDEX)
                    .findFirst().orElse(null);
            if (assignNode == null) return;

            membersGridPane.getChildren().remove(assignNode);
            Label label = new Label(userOptional.get().getCustomRole().getName());
            applyStandardMargin(label);
            membersGridPane.add(label, ASSIGN_ROLE_COLUMN_INDEX, editedUserRowId);
            GridPane.setHalignment(label, HPos.CENTER);

            editedUserRowId = -1;
        });
        return userOptional;
    }

    private Optional<User> handleRoleException(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to assign role");
        return Optional.empty();
    }

    private void cancelAssignRole() {
        assignRolePopup.hide();
    }

    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
    }


    // TODO: Take this into separate class and reuse
    private void styleAddNewRoleButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(plusImage));
        button.setText("Add New Member");
        applyStandardMargin(button);
    }

    private void styleDeleteRoleButton(Button button) {
        button.getStyleClass().add("standard-delete-button");
        button.setGraphic(createImageView(trashImage));
        button.setTooltip(new Tooltip("Delete Member"));
        GridPane.setMargin(button, new Insets(12));
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void applyStandardMargin(Node node) {
        GridPane.setMargin(node, new Insets(12, 36, 12, 36));
    }
}
