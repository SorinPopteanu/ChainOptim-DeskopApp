package org.chainoptim.desktop.features.supply.controller;

import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.features.supply.performance.model.ComponentDeliveryPerformance;
import org.chainoptim.desktop.features.supply.performance.model.SupplierPerformance;
import org.chainoptim.desktop.features.supply.performance.model.SupplierPerformanceReport;
import org.chainoptim.desktop.features.supply.performance.service.SupplierPerformanceService;
import org.chainoptim.desktop.features.supply.model.Supplier;
import org.chainoptim.desktop.shared.common.uielements.info.InfoLabel;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchData;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.TimeUtil;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.*;
import java.util.*;

public class SupplierPerformanceController implements DataReceiver<SearchData<Supplier>> {

    // Services
    private final SupplierPerformanceService supplierPerformanceService;

    // State
    private final FallbackManager fallbackManager;
    private SupplierPerformance supplierPerformance;

    // FXML
    @FXML
    private InfoLabel supplierPerformanceInfoLabel;
    @FXML
    private Button refreshReportButton;
    @FXML
    private Label totalDeliveredOrders;
    @FXML
    private Label totalDelays;
    @FXML
    private Label averageDelayPerOrder;
    @FXML
    private Label averageTimeToShipOrder;
    @FXML
    private Label onTimeOrdersPercentage;
    @FXML
    private ComboBox<Pair<Integer, String>> componentsComboBox; // Component ID, Component Name
    @FXML
    private Label componentTotalDeliveredOrders;
    @FXML
    private Label totalDeliveredQuantity;
    @FXML
    private Label averageDeliveredQuantity;
    @FXML
    private Label averageOrderQuantity;
    @FXML
    private Label averageShipmentQuantity;
    @FXML
    private Label deliveredPerOrderedPercentage;
    @FXML
    private LineChart<String, Number> lineChart;

    @Inject
    public SupplierPerformanceController(SupplierPerformanceService supplierPerformanceService,
                                         FallbackManager fallbackManager) {
        this.supplierPerformanceService = supplierPerformanceService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(SearchData<Supplier> searchData) {
        setUpInfoLabel();
        setUpRefreshButton(searchData.getData().getId());
        setUpComponentsComboBox();
        loadSupplierPerformance(searchData.getData().getId(), false);
    }

    private void setUpInfoLabel() {
        supplierPerformanceInfoLabel.setFeatureAndLevel(Feature.SUPPLIER_PERFORMANCE,
                TenantSettingsContext.getCurrentUserSettings().getGeneralSettings().getInfoLevel());
    }

    private void setUpRefreshButton(Integer supplierId) {
        ImageView refreshIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png"))));
        refreshIcon.setFitWidth(16);
        refreshIcon.setFitHeight(16);
        refreshReportButton.setGraphic(refreshIcon);
        refreshReportButton.setText("Refresh Report");
        refreshReportButton.setOnAction(event -> loadSupplierPerformance(supplierId, true));
    }

    private void setUpComponentsComboBox() {
        // Make combo box only display component name
        componentsComboBox.setCellFactory(lv -> new ListCell<Pair<Integer, String>>() {
            @Override
            protected void updateItem(Pair<Integer, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getValue());
            }
        });
        componentsComboBox.setButtonCell(new ListCell<Pair<Integer, String>>() {
            @Override
            protected void updateItem(Pair<Integer, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getValue());
            }
        });

        // Listen to component selection and update component UI accordingly
        componentsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedComponentId = newValue.getKey();
                updateComponentUI(supplierPerformance.getReport(), selectedComponentId);
            }
        });
    }

    private void loadSupplierPerformance(Integer supplierId, boolean refresh) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        supplierPerformanceService.getSupplierPerformanceBySupplierId(supplierId, refresh)
                .thenApply(this::handlePerformanceResponse)
                .exceptionally(this::handlePerformanceException);
    }

    private Result<SupplierPerformance> handlePerformanceResponse(Result<SupplierPerformance> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Supplier performance not found");
                return;
            }
            this.supplierPerformance = result.getData();
            fallbackManager.setLoading(false);

            displayReport(supplierPerformance);
        });
        return result;
    }

    private Result<SupplierPerformance> handlePerformanceException(Throwable ex) {
        System.out.println("Supplier performance exception: " + ex.getMessage());
        return new Result<>();
    }

    private void displayReport(SupplierPerformance supplierPerformance) {
        totalDeliveredOrders.setText(Integer.toString(supplierPerformance.getReport().getTotalDeliveredOrders()));
        totalDelays.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getTotalDelays()));
        averageDelayPerOrder.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getAverageDelayPerOrder()));
        averageTimeToShipOrder.setText(TimeUtil.formatDuration(supplierPerformance.getReport().getAverageTimeToShipOrder()));
        onTimeOrdersPercentage.setText(supplierPerformance.getReport().getRatioOfOnTimeOrderDeliveries() * 100 + "%");

        componentsComboBox.getItems().clear();
        for (Map.Entry<Integer, ComponentDeliveryPerformance> entry : supplierPerformance.getReport().getComponentPerformances().entrySet()) {
            componentsComboBox.getItems().add(new Pair<>(entry.getKey(), entry.getValue().getComponentName()));
        }
        // Select first (triggering component UI update)
        componentsComboBox.getSelectionModel().selectFirst();
    }

    private void updateComponentUI(SupplierPerformanceReport report, int componentId) {
        ComponentDeliveryPerformance componentDeliveryPerformance = report.getComponentPerformances().get(componentId);

        if (componentDeliveryPerformance == null) {
            System.out.println("Component not found in report: " + componentId);
            return;
        }

        componentTotalDeliveredOrders.setText(Float.toString(componentDeliveryPerformance.getTotalDeliveredOrders()));
        totalDeliveredQuantity.setText(Float.toString(componentDeliveryPerformance.getTotalDeliveredQuantity()));
        averageDeliveredQuantity.setText(Float.toString(componentDeliveryPerformance.getAverageDeliveredQuantity()));
        averageOrderQuantity.setText(Float.toString(componentDeliveryPerformance.getAverageOrderQuantity()));
        averageShipmentQuantity.setText(Float.toString(componentDeliveryPerformance.getAverageShipmentQuantity()));
        deliveredPerOrderedPercentage.setText(componentDeliveryPerformance.getDeliveredPerOrderedRatio() * 100 + "%");

        plotData(componentDeliveryPerformance.getFirstDeliveryDate(), componentDeliveryPerformance.getDeliveredQuantityOverTime());
    }

    private void plotData(LocalDateTime firstDeliveryDate, Map<Float, Float> deliveredQuantityOverTime) {
        lineChart.getData().clear();
        lineChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate startDate = firstDeliveryDate.toLocalDate();
        float maxDays = Collections.max(deliveredQuantityOverTime.keySet());
        LocalDate endDate = startDate.plusDays((long) maxDays);

        // Prepare the list of all month-year labels to be used as categories
        List<String> allLabels = new ArrayList<>();
        LocalDate currentMonth = startDate.withDayOfMonth(1);
        while (!currentMonth.isAfter(endDate)) {
            String monthYear = currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            allLabels.add(monthYear);
            currentMonth = currentMonth.plusMonths(1);
        }

        CategoryAxis xAxis = (CategoryAxis) lineChart.getXAxis();
        xAxis.setCategories(FXCollections.observableArrayList(allLabels));

        // Plot the data
        currentMonth = startDate.withDayOfMonth(1);
        for (String label : allLabels) {
            LocalDate nextMonth = currentMonth.plusMonths(1);
            Float sumValueForMonth = 0f;
            int count = 0;
            for (Map.Entry<Float, Float> entry : deliveredQuantityOverTime.entrySet()) {
                LocalDate entryDate = startDate.plusDays(entry.getKey().longValue());
                if (!entryDate.isBefore(currentMonth) && entryDate.isBefore(nextMonth)) {
                    sumValueForMonth += entry.getValue();
                    count++;
                }
            }
            float averageValueForMonth = count > 0 ? sumValueForMonth / count : 0;
            series.getData().add(new XYChart.Data<>(label, averageValueForMonth));

            currentMonth = nextMonth; // Advance to the next month
        }

        lineChart.getData().add(series);
    }
}
