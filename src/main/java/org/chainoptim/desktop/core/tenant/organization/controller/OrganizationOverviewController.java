package org.chainoptim.desktop.core.tenant.organization.controller;

import org.chainoptim.desktop.core.main.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.main.service.CurrentSelectionService;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.tenant.customrole.controller.OrganizationAssignBasicRoleController;
import org.chainoptim.desktop.core.tenant.customrole.controller.OrganizationAssignCustomRoleController;
import org.chainoptim.desktop.core.tenant.customrole.model.CustomRole;
import org.chainoptim.desktop.core.tenant.organization.model.OrganizationViewData;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.core.tenant.user.service.UserService;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.common.ui.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;
import java.awt.Desktop;

public class OrganizationOverviewController implements DataReceiver<OrganizationViewData> {

    // Services
    private final UserService userService;
    private final NavigationService navigationService;
    private final CurrentSelectionService currentSelectionService;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    // Listeners
    private RunnableConfirmDialogActionListener<Pair<String, Integer>> customRoleUpdateDialogListener; // Pair (userId, customRoleId)
    private RunnableConfirmDialogActionListener<Pair<String, User.Role>> basicRoleUpdateDialogListener; // Custom Role name
    private RunnableConfirmDialogActionListener<Pair<String, Integer>> removeMemberDialogListener; // Pair (userId, organizationId)

    // State
    private OrganizationViewData organizationViewData;
    private boolean isInitialRender = true;
    private int editedUserRowIndex = -1; // Marker for no edit
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
    private Popup assingBasicRolePopup;
    private Popup assignCustomRolePopup;
    @FXML
    private StackPane removeConfirmDialogPane;

    // Constants
    private static final String[] headers = {"Username", "Joined At", "Email", "Role", "Custom Role"};
    private static final int ASSIGN_BASIC_ROLE_COLUMN_INDEX = 3;
    private static final int ASSIGN_ROLE_COLUMN_INDEX = 4;
    private static final int DELETE_COLUMN_INDEX = 5;

    // Icons
    private Image plusImage;
    private Image trashImage;

    @Inject
    public OrganizationOverviewController(UserService userService,
                                          NavigationService navigationService,
                                          CurrentSelectionService currentSelectionService,
                                          FXMLLoaderService fxmlLoaderService,
                                          ControllerFactory controllerFactory,
                                          FallbackManager fallbackManager) {
        this.userService = userService;
        this.navigationService = navigationService;
        this.currentSelectionService = currentSelectionService;
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
        addNewMemberButton.setOnAction(event -> handleAddNewMembers());
    }

    private void setupListeners() {
        // Assign Custom Role
        Consumer<Pair<String, Integer>> onConfirmUpdate = this::assignCustomRole;
        Runnable onCancelUpdate = this::cancelAssignCustomRole;

        customRoleUpdateDialogListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);

        // Assign Basic Role
        Consumer<Pair<String, User.Role>> onConfirmBasicRoleUpdate = this::assignBasicRole;
        Runnable onCancelBasicRoleUpdate = this::cancelAssignBasicRole;

        basicRoleUpdateDialogListener = new RunnableConfirmDialogActionListener<>(onConfirmBasicRoleUpdate, onCancelBasicRoleUpdate);

        // Remove organization member
        Consumer<Pair<String, Integer>> onConfirmDelete = this::removeOrganizationMember;
        Runnable onCancelDelete = this::cancelRemoveMember;

        removeMemberDialogListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

    }

    // Render UI
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
                if (i > 0 && i != 2) applyStandardMargin(node); // Skip for username and email
                membersGridPane.add(node, i, row);
                GridPane.setHalignment(node, HPos.CENTER);
            }

            addRemoveMemberButton(user, row);

            row++;
        }
    }

    private Node getDisplayedPropertyByHeader(User user, int rowIndex, int headerIndex) {
        String header = headers[headerIndex];
        return switch (header) {
            case "Username" -> {
                Label label = new Label(user.getUsername());
                label.getStyleClass().add("parent-row");
                yield label;
            }
            case "Joined At" -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                String formattedDate = user.getCreatedAt().format(formatter);
                yield new Label(formattedDate);
            }
            case "Email" -> addEmailHyperlink(user);
            case "Role" -> addBasicRoleButton(user, rowIndex);
            case "Custom Role" -> addCustomRoleButton(user, rowIndex);
            default -> null;
        };
    }

    private Hyperlink addEmailHyperlink(User user) {
        Hyperlink emailLink = new Hyperlink(user.getEmail());
        emailLink.getStyleClass().clear();
        emailLink.getStyleClass().add("pseudo-link");
        emailLink.setOnAction(event -> {
            // Open default email client
            try {
                Desktop.getDesktop().mail(new URI("mailto:" + user.getEmail()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        return emailLink;
    }

    private Button addBasicRoleButton(User user, Integer rowIndex) {
        String label = user.getRole() != null ? user.getRole().toString() : "Assign";
        // Decapitalize except first letter
        label = label.substring(0, 1).toUpperCase() + label.substring(1).toLowerCase();
        Button button = new Button(label);
        button.setOnAction(event -> {
            editedUserRowIndex = rowIndex;
            loadAssignBasicRoleDialog(user, event);
        });
        button.getStyleClass().add("pseudo-link");
        return button;
    }

    private Button addCustomRoleButton(User user, Integer rowIndex) {
        String label = user.getCustomRole() != null ? user.getCustomRole().getName() : "Assign";
        Button button = new Button(label);
        button.getStyleClass().add("pseudo-link");
        button.setOnAction(event -> {
            editedUserRowIndex = rowIndex;
            loadAssignCustomRoleDialog(user, event, user.getCustomRole());
        });
        return button;
    }

    private void addRemoveMemberButton(User user, int rowIndex) {
        Button removeButton = new Button();
        styleDeleteRoleButton(removeButton);
        removeButton.setOnAction(event -> loadConfirmDeleteDialog(user));
        applyStandardMargin(removeButton);
        removeButton.setVisible(false); // Hide it initially
        membersGridPane.add(removeButton, DELETE_COLUMN_INDEX, rowIndex);
        GridPane.setHalignment(removeButton, HPos.CENTER);
    }

    // Event handlers
    // - Assign Basic Role
    private void loadAssignBasicRoleDialog(User user, ActionEvent event) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/OrganizationAssignBasicRoleView.fxml",
                controllerFactory::createController
        );
        try {
            Node content = loader.load();
            OrganizationAssignBasicRoleController controller = loader.getController();
            controller.setData(user);
            controller.setActionListener(basicRoleUpdateDialogListener);

            assingBasicRolePopup = new Popup();
            assingBasicRolePopup.getContent().add(content);
            assingBasicRolePopup.setAutoHide(true);

            Button sourceButton = (Button) event.getSource(); // Safe cast, source is a Button
            double x = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxX(); // Open to the left
            double y = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxY();
            assingBasicRolePopup.show(sourceButton, x, y);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void assignBasicRole(Pair<String, User.Role> userIdRole) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        userService.assignBasicRoleToUser(userIdRole.getKey(), userIdRole.getValue())
                .thenApply(this::handleBasicRoleResponse)
                .exceptionally(this::handleBasicRoleException);
    }

    private Result<User> handleBasicRoleResponse(Result<User> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to assign basic role.");
                return;
            }
            fallbackManager.setLoading(false);

            // Update Users list with updated user
            User updatedUser = result.getData();
            organizationViewData.getOrganization().getUsers().stream()
                    .filter(user -> user.getId().equals(updatedUser.getId()))
                    .findFirst().ifPresent(user -> user.setRole(updatedUser.getRole()));

            // Update Custom Role button
            Node assignNode = membersGridPane.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == editedUserRowIndex && GridPane.getColumnIndex(node) == ASSIGN_BASIC_ROLE_COLUMN_INDEX)
                    .findFirst().orElse(null);
            if (assignNode == null) return;

            membersGridPane.getChildren().remove(assignNode);
            Button button = addBasicRoleButton(updatedUser, editedUserRowIndex);
            applyStandardMargin(button);
            membersGridPane.add(button, ASSIGN_BASIC_ROLE_COLUMN_INDEX, editedUserRowIndex);
            GridPane.setHalignment(button, HPos.CENTER);

            editedUserRowIndex = -1;
        });
        return result;
    }

    private Result<User> handleBasicRoleException(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to assign basic role.");
        return new Result<>();
    }

    private void cancelAssignBasicRole() {
        assingBasicRolePopup.hide();
    }

    // - Assign Custom Role
    private void loadAssignCustomRoleDialog(User user, ActionEvent event, CustomRole selectedRole) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(
                "/org/chainoptim/desktop/core/organization/OrganizationAssignCustomRoleView.fxml",
                controllerFactory::createController
        );
        try {
            Node content = loader.load();
            OrganizationAssignCustomRoleController controller = loader.getController();
            controller.setData(new Pair<>(user, new Pair<>(organizationViewData.getCustomRoles(), selectedRole)));
            controller.setActionListener(customRoleUpdateDialogListener);

            assignCustomRolePopup = new Popup();
            assignCustomRolePopup.getContent().add(content);
            assignCustomRolePopup.setAutoHide(true);

            Button sourceButton = (Button) event.getSource(); // Safe cast, source is a Button
            double x = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxX(); // Open to the left
            double y = sourceButton.localToScreen(sourceButton.getBoundsInLocal()).getMaxY();
            assignCustomRolePopup.show(sourceButton, x, y);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void assignCustomRole(Pair<String, Integer> userIdCustomRoleId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        userService.assignCustomRoleToUser(userIdCustomRoleId.getKey(), userIdCustomRoleId.getValue())
                .thenApply(this::handleCustomRoleResponse)
                .exceptionally(this::handleCustomRoleException);
    }

    private Result<User> handleCustomRoleResponse(Result<User> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to assign custom role.");
                return;
            }
            fallbackManager.setLoading(false);

            // Update users list with updated user
            User updatedUser = result.getData();
            organizationViewData.getOrganization().getUsers().stream()
                    .filter(user -> user.getId().equals(updatedUser.getId()))
                    .findFirst().ifPresent(user -> user.setCustomRole(updatedUser.getCustomRole()));

            // Update Custom Role button
            Node assignNode = membersGridPane.getChildren().stream()
                    .filter(node -> GridPane.getRowIndex(node) == editedUserRowIndex && GridPane.getColumnIndex(node) == ASSIGN_ROLE_COLUMN_INDEX)
                    .findFirst().orElse(null);
            if (assignNode == null) return;

            membersGridPane.getChildren().remove(assignNode);
            Button button = addCustomRoleButton(updatedUser, editedUserRowIndex);
            applyStandardMargin(button);
            membersGridPane.add(button, ASSIGN_ROLE_COLUMN_INDEX, editedUserRowIndex);
            GridPane.setHalignment(button, HPos.CENTER);

            editedUserRowIndex = -1;
        });
        return result;
    }

    private Result<User> handleCustomRoleException(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to assign custom role.");
        return new Result<>();
    }

    private void cancelAssignCustomRole() {
        assignCustomRolePopup.hide();
    }

    // - Delete
    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
        for (int row = 1; row < membersGridPane.getRowCount(); row++) {
            Node removeButton = getNodeByRowColumnIndex(row, DELETE_COLUMN_INDEX);
            if (removeButton instanceof Button button) {
                button.setVisible(isDeleteMode);
            }
        }
    }

    private void loadConfirmDeleteDialog(User user) {
        // Load confirm dialog
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/shared/common/ui/confirmdialog/GenericConfirmDialogView.fxml", controllerFactory::createController);

        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput("Confirm Member Removal", "Are you sure you want to remove " + user.getUsername() + "?", null);

        try {
            Node view = loader.load();
            GenericConfirmDialogController<Pair<String, Integer>> controller = loader.getController();
            controller.setData(new Pair<>(user.getId(), organizationViewData.getOrganization().getId()), confirmDialogInput);
            controller.setActionListener(removeMemberDialogListener);
            removeConfirmDialogPane.getChildren().add(view);
            toggleRemoveDialog(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void removeOrganizationMember(Pair<String, Integer> userIdOrganizationId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        userService.removeUserFromOrganization(userIdOrganizationId.getKey(), userIdOrganizationId.getValue())
                .thenApply(this::handleRemoveMemberResponse)
                .exceptionally(this::handleRemoveMemberException);
    }

    private Result<User> handleRemoveMemberResponse(Result<User> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to remove member.");
                return;
            }
            User user = result.getData();

            toggleRemoveDialog(false);
            fallbackManager.setLoading(false);
            toggleDeleteMode();

            // Remove user from organization
            organizationViewData.getOrganization().getUsers().removeIf(u -> u.getId().equals(user.getId()));

            // Re-render members grid - necessary here, at least from that user down
            renderMembersGrid();
        });
        return result;
    }

    private Result<User> handleRemoveMemberException(Throwable throwable) {
        fallbackManager.setErrorMessage("Failed to remove member.");
        return new Result<>();
    }

    private void cancelRemoveMember() {
        toggleRemoveDialog(false);
    }

    private void toggleRemoveDialog(boolean isActive) {
        removeConfirmDialogPane.setVisible(isActive);
        removeConfirmDialogPane.setManaged(isActive);
    }

    // Add New Members
    private void handleAddNewMembers() {
        Integer organizationId = organizationViewData.getOrganization().getId();
        currentSelectionService.setSelectedId(organizationId);
        navigationService.switchView("Add-New-Members?id=" + organizationId, true, null);
    }

    // Utils
    private Node getNodeByRowColumnIndex(final int row, final int column) {
        Node result = null;
        for (Node node : membersGridPane.getChildren()) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    // Styling
    // TODO: Take this into separate class and reuse
    private void styleAddNewRoleButton(Button button) {
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(plusImage));
        button.setText("Add New Members");
        applyStandardMargin(button);
    }

    private void styleDeleteRoleButton(Button button) {
        button.getStyleClass().add("standard-delete-button");
        button.setGraphic(createImageView(trashImage));
        button.setTooltip(new Tooltip("Remove Member"));
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
