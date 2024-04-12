package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionhistory.model.DailyProductionRecord;
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
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;
import javafx.util.StringConverter;

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
    private int selectedComponentId;
    private String selectedDuration = "3 Months";
    private Map<String, Boolean> seriesVisibilityMap = new HashMap<>();

    // Constants
    private static final List<String> DURATION_OPTIONS = List.of("1 Week", "1 Month", "3 Months", "1 Year", "2 Years", "5 Years", "All Time");
    private static final List<String> SERIES_TYPES = List.of("Planned", "Allocated", "Requested");

    // FXML
    @FXML
    private ComboBox<Pair<Integer, String>> componentsComboBox; // Component ID, Component Name
    @FXML
    private AreaChart<Number, Number> areaChart;
    @FXML
    private ComboBox<String> chartDurationComboBox;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private StackPane legendPane;

    @Inject
    public FactoryPerformanceController(FactoryProductionHistoryService productionHistoryService, FallbackManager fallbackManager) {
        this.productionHistoryService = productionHistoryService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        setUpComponentsComboBox();
        setUpChartDurationComboBox();
        loadProductionHistory(factory.getId());
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
                selectedComponentId = newValue.getKey();
                updateComponentAreaUI(factoryProductionHistory.getProductionHistory());
                updateComponentUI(factoryProductionHistory.getProductionHistory());
            }
        });
    }

    private void setUpChartDurationComboBox() {
        chartDurationComboBox.getItems().addAll(DURATION_OPTIONS);
        chartDurationComboBox.getSelectionModel().select(selectedDuration);
        chartDurationComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedDuration = newValue;
                updateComponentAreaUI(factoryProductionHistory.getProductionHistory());
                applySeriesVisibility();
                updateComponentUI(factoryProductionHistory.getProductionHistory());
            }
        });
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

            displayHistory(factoryProductionHistory.getProductionHistory());
        });
        return productionHistoryOptional;
    }

    private void displayHistory(ProductionHistory history) {
        componentsComboBox.getItems().clear();

        // Set up initial visibility of Series Types
        seriesVisibilityMap.put(SERIES_TYPES.get(0), false);
        seriesVisibilityMap.put(SERIES_TYPES.get(1), true);
        seriesVisibilityMap.put(SERIES_TYPES.get(2), true);

        // Find components
        for (Map.Entry<Float, DailyProductionRecord> entry : history.getDailyProductionRecords().entrySet()) {
            for (ResourceAllocation allocation : entry.getValue().getAllocations()) {
                if (componentsComboBox.getItems().stream().noneMatch(pair -> pair.getKey().equals(allocation.getComponentId()))) {
                    componentsComboBox.getItems().add(new Pair<>(allocation.getComponentId(), allocation.getComponentName()));
                }
            }
        }

        // Select first to trigger update
        componentsComboBox.getSelectionModel().selectFirst();

        applySeriesVisibility();
    }

    private void updateComponentAreaUI(ProductionHistory history) {
        areaChart.getData().clear();

        float chartStart = determineChartStart();

        // Draw series for each daily record
        for (Map.Entry<Float, DailyProductionRecord> entry : history.getDailyProductionRecords().entrySet()) {
            Float daysSinceStart = entry.getKey();
            DailyProductionRecord dailyProductionRecord = entry.getValue();

            LocalDate recordStartDate = LocalDate.from(history.getStartDate().plusDays(daysSinceStart.longValue()));
            LocalDate recordEndDate = recordStartDate.plusDays((long) dailyProductionRecord.getDurationDays());

            Number requestedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, true, dailyProductionRecord.getDurationDays());
            createAndStyleSeries(SERIES_TYPES.get(2), "custom-area-line-secondary", "custom-area-fill-secondary", "custom-node-secondary",
                    recordStartDate, recordEndDate, requestedValue);

            Number allocatedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, false, dailyProductionRecord.getDurationDays());
            createAndStyleSeries(SERIES_TYPES.get(1), "custom-area-line-primary", "custom-area-fill-primary", "custom-node-primary",
                    recordStartDate, recordEndDate, allocatedValue);

            Number plannedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, false, dailyProductionRecord.getDurationDays());
            createAndStyleSeries(SERIES_TYPES.get(0), "custom-area-line-tertiary", "custom-area-fill-tertiary", "custom-node-tertiary",
                    recordStartDate, recordEndDate, plannedValue);
        }

        Platform.runLater(this::setupCustomLegend);

        configureXAxis(chartStart);
    }

    private long determineChartStart() {
        long chartStart = 0;
        LocalDate endDate = LocalDate.now();
        chartStart = switch (selectedDuration) {
            case "1 Week" -> endDate.minusWeeks(1).toEpochDay();
            case "1 Month" -> endDate.minusMonths(1).toEpochDay();
            case "3 Months" -> endDate.minusMonths(3).toEpochDay();
            case "1 Year" -> endDate.minusYears(1).toEpochDay();
            case "2 Years" -> endDate.minusYears(2).toEpochDay();
            case "5 Years" -> endDate.minusYears(5).toEpochDay();
            case "All Time" -> 0;
            default -> chartStart;
        };

        return chartStart;
    }

    private Number findValueInAllocations(List<ResourceAllocation> allocations, Integer componentId, boolean isRequested, float durationDays) {
        return allocations.stream()
                .filter(alloc -> alloc.getComponentId().equals(componentId))
                .findFirst()
                .map(alloc -> isRequested ? alloc.getRequestedAmount() : alloc.getAllocatedAmount())
                .map(value -> durationDays != 0 ? value / durationDays : 0f) // Normalize to daily value
                .orElse(0f);
    }

    private void createAndStyleSeries(
            String seriesName, String lineStyleClass, String fillStyleClass, String nodeStyleClass,
            LocalDate startDate, LocalDate endDate, Number value) {

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        series.getData().add(new XYChart.Data<>(startDate.toEpochDay(), value));
        series.getData().add(new XYChart.Data<>(endDate.toEpochDay(), value));

        areaChart.getData().add(series);

        // Style series
        Platform.runLater(() -> {
            styleChartSeries(series, lineStyleClass, fillStyleClass);
            styleChartNodes(series, nodeStyleClass);
        });
    }

    private void configureXAxis(Float chartStart) {
        NumberAxis xAxis = (NumberAxis) areaChart.getXAxis();

        LocalDate currentDate = LocalDate.now();
        // Disable auto-ranging to manually set the bounds: chart start to today
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(chartStart);
        xAxis.setUpperBound(currentDate.toEpochDay());

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // Convert the day offset back to a date for display
                LocalDate date = LocalDate.ofEpochDay(object.longValue());
                return date.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            }

            @Override
            public Number fromString(String string) {
                return LocalDate.parse(string, DateTimeFormatter.ofPattern("MMM yyyy")).toEpochDay();
            }
        });
    }

    private void styleChartSeries(XYChart.Series<Number, Number> series, String lineClass, String fillClass) {
        Node line = series.getNode().lookup(".chart-series-line");
        if (line != null) {
            line.getStyleClass().add(lineClass);
        }

        Node fill = series.getNode().lookup(".chart-series-area-fill");
        if (fill != null) {
            fill.getStyleClass().add(fillClass);
        }
    }

    private void styleChartNodes(XYChart.Series<Number, Number> series, String cssClass) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("");
                node.getStyleClass().add(cssClass);
            }
        }
    }

    private void setupCustomLegend() {
        HBox legendContainer = new HBox(10);

        for (String seriesType : SERIES_TYPES) {
            Label label = new Label(seriesType);
            Circle symbol = new Circle(5);
            symbol.setStroke(getColorByLabel(seriesType));
            boolean isVisible = seriesVisibilityMap.getOrDefault(seriesType, true);
            symbol.setFill(isVisible ? symbol.getStroke() : Color.TRANSPARENT);
            label.setGraphic(symbol);
            label.setContentDisplay(ContentDisplay.RIGHT);

            label.setOnMouseClicked(event -> toggleSeriesVisibility(seriesType, symbol));

            legendContainer.getChildren().add(label);
        }

        legendPane.getChildren().clear();
        legendPane.getChildren().add(legendContainer);
    }

    private void applySeriesVisibility() {
        for (Map.Entry<String, Boolean> entry : seriesVisibilityMap.entrySet()) {
            String seriesType = entry.getKey();
            boolean isVisible = entry.getValue();
            areaChart.getData().stream()
                    .filter(s -> s.getName().equals(seriesType))
                    .forEach(s -> {
                        s.getNode().setVisible(isVisible);
                        s.getData().forEach(data -> {
                            if (data.getNode() != null) {
                                data.getNode().setVisible(isVisible);
                            }
                        });
                    });
            // Update legend circle color based on visibility state
            updateLegendCircleColor(seriesType, isVisible);
        }
    }

    private void updateLegendCircleColor(String seriesType, boolean isVisible) {
        legendPane.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .filter(label -> label.getText().equals(seriesType))
                .findFirst()
                .ifPresent(label -> {
                    Circle symbol = (Circle) label.getGraphic();
                    symbol.setFill(isVisible ? symbol.getStroke() : Color.TRANSPARENT);
                });
    }

    private void toggleSeriesVisibility(String seriesType, Circle symbol) {
        boolean isVisible = seriesVisibilityMap.get(seriesType);
        boolean newVisibility = !isVisible;
        seriesVisibilityMap.put(seriesType, newVisibility);

        areaChart.getData().stream()
                .filter(s -> s.getName().equals(seriesType))
                .forEach(s -> {
                    s.getNode().setVisible(newVisibility);
                    s.getData().forEach(data -> {
                        if (data.getNode() != null) {
                            data.getNode().setVisible(newVisibility);
                        }
                    });
                    symbol.setFill(newVisibility ? symbol.getStroke() : Color.TRANSPARENT);
                });

        updateLegendCircleColor(seriesType, newVisibility);
    }

    private Color getColorByLabel(String label) {
        return switch (label) {
            case "Planned" -> Color.valueOf("#4CAF50");
            case "Allocated" -> Color.valueOf("#006AEE");
            case "Requested" -> Color.valueOf("#FF9800");
            default -> Color.BLACK;
        };
    }

    // Line Chart
    private void updateComponentUI(ProductionHistory history) {
        Map<Float, Pair<Float, Float>> dataOverTime = history.getDailyProductionRecords().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            var allocations = entry.getValue().getAllocations().stream()
                                    .filter(alloc -> alloc.getComponentId().equals(selectedComponentId))
                                    .findFirst()
                                    .orElse(new ResourceAllocation());

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
        requestedSeries.setName("Needed Amount");
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
                    case "Needed Amount":
                        series.getNode().setStyle("-fx-stroke: blue;");
                        break;
                    case "Allocated Amount":
                        series.getNode().setStyle("-fx-stroke: orange;");
                        break;
                    default:
                        break;
                }
            }

            // Add event listener on legend items to toggle visibility of series
            for (Node node : lineChart.lookupAll(".chart-legend-item")) {
                node.setOnMouseClicked(mouseEvent -> {
                    for (XYChart.Series<String, Number> s : lineChart.getData()) {
                        if (s.getName().equals(((Label) node).getText())) {
                            s.getNode().setVisible(!s.getNode().isVisible());
                            s.getData().forEach(data -> {
                                Node dataNode = data.getNode();
                                if (dataNode != null) {
                                    dataNode.setVisible(s.getNode().isVisible());
                                }
                            });
                        }
                    }
                });
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
