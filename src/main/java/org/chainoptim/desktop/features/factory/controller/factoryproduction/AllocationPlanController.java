package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.util.Objects;

public class AllocationPlanController {

    // State
    private AllocationPlan allocationPlan;

    // FXML
    @FXML
    private Button saveButton;
    @FXML
    private Button usePlanButton;
    @FXML
    private TableView<ResourceAllocation> tableView;
    @FXML
    private TableColumn<ResourceAllocation, Float> allocatedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> requestedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> deficitColumn;
    @FXML
    private TableColumn<ResourceAllocation, String> deficitPercentageColumn;
    @FXML
    private TableColumn<ResourceAllocation, String> componentNameColumn;

    // Icons
    private Image saveImage;

    public void initialize(AllocationPlan allocationPlan) {
        this.allocationPlan = allocationPlan;
        System.out.println("AllocationPlanController.initialize: " + allocationPlan);
        initializeIcons();
        styleUsePlanButton(usePlanButton);
        styleSaveButton(saveButton);
        displayAllocations();
    }

    private void initializeIcons() {
        saveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/floppy-disk-solid.png")));
    }

    private void displayAllocations() {
        allocatedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));
        deficitColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            return new SimpleFloatProperty(deficit).asObject();
        });
        deficitPercentageColumn.setCellValueFactory(cellData -> {
            ResourceAllocation allocation = cellData.getValue();
            float requestedAmount = allocation.getRequestedAmount();
            float deficit = allocation.getRequestedAmount() - allocation.getAllocatedAmount();
            float ratio = deficit / requestedAmount;
            return new SimpleStringProperty(ratio * 100 + "%");
        });
        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("componentName"));

        // Change the style of the deficit column based on the ratio of the deficit to the requested amount
        setupDeficitStyling(deficitColumn, false);
        setupDeficitStyling(deficitPercentageColumn, true);

        tableView.getItems().setAll(allocationPlan.getAllocations());
    }


    private <T> void setupDeficitStyling(TableColumn<ResourceAllocation, T> column, boolean isPercentage) {
        column.setCellFactory(col -> new TableCell<ResourceAllocation, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("good-label", "average-label", "bad-label");
                } else {
                    ResourceAllocation allocation = getTableView().getItems().get(getIndex());
                    float requestedAmount = allocation.getRequestedAmount();
                    float allocatedAmount = allocation.getAllocatedAmount();
                    float deficit = requestedAmount - allocatedAmount;
                    float ratioOrPercentage = isPercentage ? (deficit / requestedAmount) * 100 : deficit / requestedAmount;

                    // Setting text based on whether it's a percentage or a ratio
                    setText(isPercentage ? String.format("%.2f%%", ratioOrPercentage) : String.format("%.2f", deficit));

                    // Remove all previous styles
                    getStyleClass().removeAll("good-label", "average-label", "bad-label");

                    // Apply new style based on the ratio or percentage
                    if (ratioOrPercentage < (isPercentage ? 10 : 0.1)) { // Adjusted thresholds for ratio vs. percentage
                        getStyleClass().add("good-label");
                    } else if (ratioOrPercentage < (isPercentage ? 20 : 0.2)) {
                        getStyleClass().add("average-label");
                    } else {
                        getStyleClass().add("bad-label");
                    }
                }
            }
        });
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }

    private void styleUsePlanButton(Button button) {
        button.setText("Use Plan");
        button.getStyleClass().add("standard-write-button");
    }

    private void styleSaveButton(Button button) {
        button.setText("Save");
        button.getStyleClass().add("standard-write-button");
        button.setGraphic(createImageView(saveImage));
    }
}
