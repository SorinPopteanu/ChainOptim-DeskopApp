package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.AllocationPlan;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Objects;

public class AllocationPlanController {

    private AllocationPlan allocationPlan;

    @FXML
    private TableView<ResourceAllocation> tableView;
    @FXML
    private TableColumn<ResourceAllocation, Float> allocatedAmountColumn;
    @FXML
    private TableColumn<ResourceAllocation, Float> requestedAmountColumn;

    public void initialize(AllocationPlan allocationPlan) {
        this.allocationPlan = allocationPlan;
        System.out.println("Allocation Plan initialized: " + allocationPlan);
        displayAllocations();
    }

    private void displayAllocations() {
        allocatedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));

        for (ResourceAllocation allocation : allocationPlan.getAllocations()) {
            if (Objects.equals(allocation.getAllocatedAmount(), allocation.getRequestedAmount())) {

            }
        }
        tableView.getItems().setAll(allocationPlan.getAllocations());
    }
}
