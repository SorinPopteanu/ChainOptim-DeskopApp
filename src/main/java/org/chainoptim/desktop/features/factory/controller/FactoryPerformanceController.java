package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.FactoryProductionHistory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.ProductionHistory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.service.FactoryProductionHistoryService;
import org.chainoptim.desktop.features.scanalysis.resourceallocation.model.ResourceAllocation;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.util.DataReceiver;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FactoryPerformanceController implements DataReceiver<Factory> {

    // Services
    private final FactoryProductionHistoryService productionHistoryService;

    // State
    private final FallbackManager fallbackManager;
    private FactoryProductionHistory factoryProductionHistory;

    // FXML
    @FXML
    private LineChart<String, Number> lineChart;

    @Inject
    public FactoryPerformanceController(FactoryProductionHistoryService productionHistoryService, FallbackManager fallbackManager) {
        this.productionHistoryService = productionHistoryService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        loadProductionHistory(factory.getId());
    }

    private void loadProductionHistory(Integer factoryId) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        productionHistoryService.getFactoryProductionHistoryByFactoryId(factoryId)
                .thenApply(this::handleProductionHistoryResponse)
                .exceptionally(this::handleProductionHistoryException);
    }

    private Optional<FactoryProductionHistory> handleProductionHistoryResponse(Optional<FactoryProductionHistory> productionHistoryOptional) {
        Platform.runLater(() -> {
            if (productionHistoryOptional.isEmpty()) {
                fallbackManager.setErrorMessage("Failed to load production history");
                return;
            }
            factoryProductionHistory = productionHistoryOptional.get();
            fallbackManager.setLoading(false);

            System.out.println("Production History: " + factoryProductionHistory);
            updateComponentUI(factoryProductionHistory.getProductionHistory(), 1);
        });
        return productionHistoryOptional;
    }

    private void updateComponentUI(ProductionHistory history, int componentId) {
        Map<Float, Pair<Float, Float>> dataOverTime = history.getDailyProductionRecords().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            var allocations = entry.getValue().getActualResourceAllocations().stream()
                                    .filter(alloc -> alloc.getComponentId().equals(componentId))
                                    .findFirst()
                                    .orElse(new ResourceAllocation()); // Consider proper handling for missing allocations

                            float requestedAmount = allocations.getRequestedAmount();
                            float allocatedAmount = allocations.getAllocatedAmount();
                            return new Pair<>(requestedAmount, allocatedAmount);
                        }));
        plotData(history.getStartDate(), dataOverTime);
    }

    private void plotData(LocalDateTime firstDeliveryDate, Map<Float, Pair<Float, Float>> dataOverTime) {
        lineChart.getData().clear();
        lineChart.setLegendVisible(true);

        // Create two series for the two types of data
        XYChart.Series<String, Number> requestedSeries = new XYChart.Series<>();
        requestedSeries.setName("Requested Amount");
        XYChart.Series<String, Number> allocatedSeries = new XYChart.Series<>();
        allocatedSeries.setName("Allocated Amount");

        LocalDate startDate = firstDeliveryDate.toLocalDate();
        float maxDays = dataOverTime.keySet().stream().max(Float::compare).orElse(0f);
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

        // Plot the data for both requested and allocated amounts
        currentMonth = startDate.withDayOfMonth(1);
        for (String label : allLabels) {
            LocalDate nextMonth = currentMonth.plusMonths(1);
            Pair<Float, Float> sumValuesForMonth = new Pair<>(0f, 0f);
            int count = 0;
            for (Map.Entry<Float, Pair<Float, Float>> entry : dataOverTime.entrySet()) {
                LocalDate entryDate = startDate.plusDays(entry.getKey().longValue());
                if (!entryDate.isBefore(currentMonth) && entryDate.isBefore(nextMonth)) {
                    sumValuesForMonth = new Pair<>(sumValuesForMonth.getKey() + entry.getValue().getKey(), sumValuesForMonth.getValue() + entry.getValue().getValue());
                    count++;
                }
            }
            float averageRequested = count > 0 ? sumValuesForMonth.getKey() / count : 0;
            float averageAllocated = count > 0 ? sumValuesForMonth.getValue() / count : 0;
            requestedSeries.getData().add(new XYChart.Data<>(label, averageRequested));
            allocatedSeries.getData().add(new XYChart.Data<>(label, averageAllocated));

            currentMonth = nextMonth; // Advance to the next month
        }

        // Add both series to the chart
        lineChart.getData().addAll(requestedSeries, allocatedSeries);

        applyCustomSeriesStyles();
    }


    private void applyCustomSeriesStyles() {
        Platform.runLater(() -> {
            for (int i = 0; i < lineChart.getData().size(); i++) {
                XYChart.Series<String, Number> series = lineChart.getData().get(i);
                switch (series.getName()) {
                    case "Requested Amount":
                        series.getNode().setStyle("-fx-stroke: blue;");
                        break;
                    case "Allocated Amount":
                        series.getNode().setStyle("-fx-stroke: orange;");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private Optional<FactoryProductionHistory> handleProductionHistoryException(Throwable ex) {
        Platform.runLater(() -> {
            fallbackManager.setErrorMessage("Failed to load production history");
            ex.printStackTrace();
        });
        return Optional.empty();
    }
}
