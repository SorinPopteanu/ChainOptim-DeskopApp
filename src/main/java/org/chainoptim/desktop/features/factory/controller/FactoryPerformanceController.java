package org.chainoptim.desktop.features.factory.controller;

import org.chainoptim.desktop.core.context.TenantSettingsContext;
import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryPerformance;
import org.chainoptim.desktop.features.scanalysis.productionperformance.model.FactoryStagePerformanceReport;
import org.chainoptim.desktop.features.scanalysis.productionperformance.service.FactoryPerformanceService;
import org.chainoptim.desktop.shared.common.uielements.info.InfoLabel;
import org.chainoptim.desktop.shared.common.uielements.performance.ScoreDisplay;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.util.DataReceiver;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class FactoryPerformanceController implements DataReceiver<Factory> {

    // Services
    private final FactoryPerformanceService factoryPerformanceService;

    // State
    private final FallbackManager fallbackManager;
    private FactoryPerformance factoryPerformance;

    // FXML
    @FXML
    private InfoLabel factoryPerformanceInfoLabel;
    @FXML
    private Button refreshReportButton;
    @FXML
    private ScoreDisplay overallScoreDisplay;
    @FXML
    private ScoreDisplay resourceDistributionScoreDisplay;
    @FXML
    private ScoreDisplay resourceReadinessScoreDisplay;
    @FXML
    private ScoreDisplay resourceUtilizationScoreDisplay;
    @FXML
    private VBox stagesVBox;
    @FXML
    private Map<Integer, VBox> stageVBoxes = new HashMap<>();

    // Icons
    private Image refreshIcon;
    private Image angleUpIcon;
    private Image angleDownIcon;

    @Inject
    public FactoryPerformanceController(FactoryPerformanceService factoryPerformanceService, FallbackManager fallbackManager) {
        this.factoryPerformanceService = factoryPerformanceService;
        this.fallbackManager = fallbackManager;
    }

    @Override
    public void setData(Factory factory) {
        initializeIcons();
        initializeButtons();
        setUpInfoLabel();
        loadFactoryPerformance(factory.getId(), false);
    }

    private void initializeIcons() {
        refreshIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png")));
        angleUpIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-up-solid.png")));
        angleDownIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/angle-down-solid.png")));
    }

    private void initializeButtons() {
        refreshReportButton.setGraphic(createImageView(refreshIcon));
        refreshReportButton.setOnAction(event -> loadFactoryPerformance(factoryPerformance.getFactoryId(), true));
    }

    private void setUpInfoLabel() {
        factoryPerformanceInfoLabel.setFeatureAndLevel(Feature.FACTORY_PERFORMANCE,
                TenantSettingsContext.getCurrentUserSettings().getGeneralSettings().getInfoLevel());
    }

    private void loadFactoryPerformance(Integer factoryId, boolean refresh) {
        fallbackManager.reset();
        fallbackManager.setLoading(true);

        factoryPerformanceService.getFactoryPerformanceByFactoryId(factoryId, refresh)
                .thenApply(this::handleFactoryPerformanceResponse)
                .exceptionally(this::handleFactoryPerformanceException);
    }

    private Result<FactoryPerformance> handleFactoryPerformanceResponse(Result<FactoryPerformance> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                fallbackManager.setErrorMessage("Failed to load factory performance");
                return;
            }
            factoryPerformance = result.getData();
            fallbackManager.setLoading(false);

            displayReport();
        });
        return result;
    }

    private Result<FactoryPerformance> handleFactoryPerformanceException(Throwable ex) {
        Platform.runLater(() ->
            fallbackManager.setErrorMessage("Failed to load factory performance"));
        return new Result<>();
    }

    private void displayReport() {
        overallScoreDisplay.setScore((int) Math.floor(factoryPerformance.getReport().getOverallScore()));
        resourceDistributionScoreDisplay.setScore(0);
        resourceReadinessScoreDisplay.setScore((int) Math.floor(factoryPerformance.getReport().getResourceReadinessScore()));
        resourceUtilizationScoreDisplay.setScore((int) Math.floor(factoryPerformance.getReport().getResourceUtilizationScore()));

        stagesVBox.getChildren().clear();
        stagesVBox.setSpacing(10);

        for (Map.Entry<Integer, FactoryStagePerformanceReport> entry : factoryPerformance.getReport().getStageReports().entrySet()) {
            displayStageReport(entry.getValue());
        }
    }

    private void displayStageReport(FactoryStagePerformanceReport stageReport) {
        VBox stageVBox = new VBox(8);

        addStageTitle(stageReport.getStageName(), stageReport.getFactoryStageId());

        stageVBox.getChildren().add(
                getScoreDisplay("Overall Score:", (int) Math.floor(stageReport.getOverallScore()))
        );
        addScoreFlowPane(
                stageVBox,
                (int) Math.floor(0.0f),
                (int) Math.floor(stageReport.getResourceReadinessScore()),
                (int) Math.floor(stageReport.getResourceUtilizationScore())
        );

        addStageField(stageVBox, "• Total Executed Stages:", String.valueOf(stageReport.getTotalExecutedStages()));
        addStageField(stageVBox, "• Average Executed Stages Per Day:", String.valueOf(stageReport.getAverageExecutedStagesPerDay()));
        addStageField(stageVBox, "• Minimum Executed Capacity Per Day:", String.valueOf(stageReport.getMinimumExecutedCapacityPerDay()));
        addStageField(stageVBox, "• Days Under Capacity Percentage:", String.valueOf(stageReport.getDaysUnderCapacityPercentage()));

        stagesVBox.getChildren().add(stageVBox);
        stageVBoxes.put(stageReport.getFactoryStageId(), stageVBox);
    }

    private void addStageTitle(String stageName, Integer factoryStageId) {
        HBox hBox = new HBox(16);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Label stageLabel = new Label("Stage: " + stageName);
        stageLabel.getStyleClass().setAll("general-label-large");
        stageLabel.setStyle("-fx-padding: 10px 0px;");
        hBox.getChildren().add(stageLabel);

        Button toggleButton = new Button();
        toggleButton.setGraphic(createImageView(angleDownIcon));
        toggleButton.getStyleClass().setAll("no-style-button");
        toggleButton.setOnAction(event -> {
            VBox stageBox = stageVBoxes.get(factoryStageId);
            if (stageBox.isVisible()) {
                stageBox.setVisible(false);
                stageBox.setManaged(false);
                toggleButton.setGraphic(createImageView(angleDownIcon));
            } else {
                stageBox.setVisible(true);
                stageBox.setManaged(true);
                toggleButton.setGraphic(createImageView(angleUpIcon));
            }
        });
        hBox.getChildren().add(toggleButton);

        stagesVBox.getChildren().add(hBox);
    }

    private void addScoreFlowPane(VBox stageVBox, int resourceDistributionScore, int resourceReadinessScore, int resourceUtilizationScore) {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);

        flowPane.getChildren().addAll(
                getScoreDisplay("Resource Distribution:", resourceDistributionScore),
                getScoreDisplay("Resource Readiness:", resourceReadinessScore),
                getScoreDisplay("Resource Utilization:", resourceUtilizationScore)
        );

        stageVBox.getChildren().add(flowPane);
    }

    private HBox getScoreDisplay(String scoreLabel, int score) {
        HBox hBox = new HBox(4);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(scoreLabel);
        label.getStyleClass().setAll("general-label-medium-large");
        ScoreDisplay scoreDisplay = new ScoreDisplay();
        scoreDisplay.setScore(score);
        hBox.getChildren().addAll(label, scoreDisplay);

        return hBox;
    }

    private void addStageField(VBox stageVBox, String label, String value) {
        HBox hBox = new HBox(4);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().setAll("general-label");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().setAll("count-label");
        hBox.getChildren().addAll(labelLabel, valueLabel);
        stageVBox.getChildren().add(hBox);
    }

    // Utils
    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        return imageView;
    }
}







// Line Chart
//private void updateComponentUI(ProductionHistory history) {
//    Map<Float, Pair<Float, Float>> dataOverTime = history.getDailyProductionRecords().entrySet().stream()
//            .collect(Collectors.toMap(
//                    Map.Entry::getKey,
//                    entry -> {
//                        var allocations = entry.getValue().getAllocations().stream()
//                                .filter(alloc -> alloc.getComponentId().equals(selectedComponentId))
//                                .findFirst()
//                                .orElse(new ResourceAllocation());
//
//                        float requestedAmount = allocations.getRequestedAmount();
//                        float allocatedAmount = allocations.getAllocatedAmount();
//                        return new Pair<>(requestedAmount, allocatedAmount);
//                    }));
//    plotData(history.getStartDate(), dataOverTime);
//}
//
//
//private void plotData(LocalDateTime firstDeliveryDate, Map<Float, Pair<Float, Float>> dataOverTime) {
//    lineChart.getData().clear();
//    lineChart.setLegendVisible(true);
//
//    // Create two series for the two types of data
//    XYChart.Series<String, Number> requestedSeries = new XYChart.Series<>();
//    requestedSeries.setName("Needed Amount");
//    XYChart.Series<String, Number> allocatedSeries = new XYChart.Series<>();
//    allocatedSeries.setName("Allocated Amount");
//
//    LocalDate startDate = firstDeliveryDate.toLocalDate();
//    float maxDays = dataOverTime.keySet().stream().max(Float::compare).orElse(0f);
//    LocalDate endDate = startDate.plusDays((long) maxDays);
//
//    // Prepare the list of all month-year labels to be used as categories
//    List<String> allLabels = new ArrayList<>();
//    LocalDate currentMonth = startDate.withDayOfMonth(1);
//    while (!currentMonth.isAfter(endDate)) {
//        String monthYear = currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
//        allLabels.add(monthYear);
//        currentMonth = currentMonth.plusMonths(1);
//    }
//
//    CategoryAxis xAxis = (CategoryAxis) lineChart.getXAxis();
//    xAxis.setCategories(FXCollections.observableArrayList(allLabels));
//
//    // Plot the data for both requested and allocated amounts
//    currentMonth = startDate.withDayOfMonth(1);
//    for (String label : allLabels) {
//        LocalDate nextMonth = currentMonth.plusMonths(1);
//        Pair<Float, Float> sumValuesForMonth = new Pair<>(0f, 0f);
//        int count = 0;
//        for (Map.Entry<Float, Pair<Float, Float>> entry : dataOverTime.entrySet()) {
//            LocalDate entryDate = startDate.plusDays(entry.getKey().longValue());
//            if (!entryDate.isBefore(currentMonth) && entryDate.isBefore(nextMonth)) {
//                sumValuesForMonth = new Pair<>(sumValuesForMonth.getKey() + entry.getValue().getKey(), sumValuesForMonth.getValue() + entry.getValue().getValue());
//                count++;
//            }
//        }
//        float averageRequested = count > 0 ? sumValuesForMonth.getKey() / count : 0;
//        float averageAllocated = count > 0 ? sumValuesForMonth.getValue() / count : 0;
//        requestedSeries.getData().add(new XYChart.Data<>(label, averageRequested));
//        allocatedSeries.getData().add(new XYChart.Data<>(label, averageAllocated));
//
//        currentMonth = nextMonth; // Advance to the next month
//    }
//
//    // Add both series to the chart
//    lineChart.getData().addAll(requestedSeries, allocatedSeries);
//
//    applyCustomSeriesStyles();
//}
//
//private void applyCustomSeriesStyles() {
//    Platform.runLater(() -> {
//        for (int i = 0; i < lineChart.getData().size(); i++) {
//            XYChart.Series<String, Number> series = lineChart.getData().get(i);
//            switch (series.getName()) {
//                case "Needed Amount":
//                    series.getNode().setStyle("-fx-stroke: blue;");
//                    break;
//                case "Allocated Amount":
//                    series.getNode().setStyle("-fx-stroke: orange;");
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        // Add event listener on legend items to toggle visibility of series
//        for (Node node : lineChart.lookupAll(".chart-legend-item")) {
//            node.setOnMouseClicked(mouseEvent -> {
//                for (XYChart.Series<String, Number> s : lineChart.getData()) {
//                    if (s.getName().equals(((Label) node).getText())) {
//                        s.getNode().setVisible(!s.getNode().isVisible());
//                        s.getData().forEach(data -> {
//                            Node dataNode = data.getNode();
//                            if (dataNode != null) {
//                                dataNode.setVisible(s.getNode().isVisible());
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    });
//}