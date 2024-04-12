package org.chainoptim.desktop.features.factory.controller.factoryproduction;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.factory.model.TabsActionListener;
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProductionHistoryController implements DataReceiver<Factory> {

    // Services
    private final FactoryProductionHistoryService productionHistoryService;

    // Listeners
    @Setter
    private TabsActionListener actionListener;

    // State
    private Factory factory;
    private final FallbackManager fallbackManager;
    private FactoryProductionHistory factoryProductionHistory;
    private int selectedComponentId;
    private String selectedDuration = "3 Months";
    private final Map<String, Boolean> seriesVisibilityMap = new HashMap<>();

    // Constants
    private static final List<String> DURATION_OPTIONS = List.of("1 Week", "1 Month", "3 Months", "1 Year", "2 Years", "5 Years", "All Time");
    private static final List<String> SERIES_TYPES = List.of("Planned", "Allocated", "Requested");

    // FXML
    @FXML
    private ComboBox<Pair<Integer, String>> componentsComboBox; // Component ID, Component Name
    @FXML
    private Button addRecordButton;
    @FXML
    private AreaChart<Number, Number> areaChart;
    @FXML
    private ComboBox<String> chartDurationComboBox;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private StackPane legendPane;

    @Inject
    public ProductionHistoryController(FactoryProductionHistoryService productionHistoryService, FallbackManager fallbackManager) {
        this.productionHistoryService = productionHistoryService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        this.factory = factory;
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

    private Optional<FactoryProductionHistory> handleProductionHistoryException(Throwable ex) {
        Platform.runLater(() -> {
            fallbackManager.setErrorMessage("Failed to load production history");
            ex.printStackTrace();
        });
        return Optional.empty();
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
        areaChart.setLegendVisible(false);

        float chartStart = determineChartStart();

        // Draw series for each daily record
        for (Map.Entry<Float, DailyProductionRecord> entry : history.getDailyProductionRecords().entrySet()) {
            Float daysSinceStart = entry.getKey();
            DailyProductionRecord dailyProductionRecord = entry.getValue();

            LocalDate recordStartDate = LocalDate.from(history.getStartDate().plusDays(daysSinceStart.longValue()));
            LocalDate recordEndDate = recordStartDate.plusDays((long) dailyProductionRecord.getDurationDays());

            Number requestedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, "Requested", dailyProductionRecord.getDurationDays());
            createAndStyleSeries(SERIES_TYPES.get(2), "custom-area-line-secondary", "custom-area-fill-secondary", "custom-node-secondary",
                    recordStartDate, recordEndDate, requestedValue);

            Number allocatedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, "Planned", dailyProductionRecord.getDurationDays());
            createAndStyleSeries(SERIES_TYPES.get(1), "custom-area-line-primary", "custom-area-fill-primary", "custom-node-primary",
                    recordStartDate, recordEndDate, allocatedValue);

            Number plannedValue = findValueInAllocations(dailyProductionRecord.getAllocations(), selectedComponentId, "Actual", dailyProductionRecord.getDurationDays());
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

    private Number findValueInAllocations(List<ResourceAllocation> allocations, Integer componentId, String amountType, float durationDays) {
        return allocations.stream()
                .filter(alloc -> alloc.getComponentId().equals(componentId))
                .findFirst()
                .map(alloc -> switch (amountType) {
                    case "Planned" -> alloc.getAllocatedAmount();
                    case "Actual" -> alloc.getActualAmount();
                    case "Requested" -> alloc.getRequestedAmount();
                    case null, default -> 0f;
                })
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

    @FXML
    private void addProductionRecord() {
        actionListener.onAddProductionRecord(factory);
    }

    private Color getColorByLabel(String label) {
        return switch (label) {
            case "Planned" -> Color.valueOf("#4CAF50");
            case "Allocated" -> Color.valueOf("#006AEE");
            case "Requested" -> Color.valueOf("#FF9800");
            default -> Color.BLACK;
        };
    }
}
