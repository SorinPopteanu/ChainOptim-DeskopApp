package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.overview.model.UpcomingEvent;
import org.chainoptim.desktop.core.overview.service.UpcomingEventService;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.SearchParams;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class TimelineController {

    // Services
    private final UpcomingEventService upcomingEventService;

    // State
    private final SearchParams searchParams;
    private List<UpcomingEvent> events;

    // FXML
    @FXML
    private ComboBox<String> timeframeComboBox;
    @FXML
    private Button refreshButton;
    @FXML
    private HBox eventsHBox;

    @Inject
    public TimelineController(UpcomingEventService upcomingEventService,
                              SearchParams searchParams) {
        this.upcomingEventService = upcomingEventService;
        this.searchParams = searchParams;
    }

    public void initializeTimeline() {
        initializeUI();
        setUpTimeframeComboBox();
        searchParams.getFiltersProperty().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            loadEvents();
        });
        timeframeComboBox.getSelectionModel().selectFirst();
    }

    private void initializeUI() {
        Image refreshIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/rotate-right-solid.png")));
        ImageView imageView = new ImageView(refreshIcon);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        refreshButton.setGraphic(imageView);
    }

    private void loadEvents() {
        upcomingEventService.getUpcomingEventsByOrganizationIdAdvanced(TenantContext.getCurrentUser().getOrganization().getId(), searchParams)
                .thenApply(this::handleEventsResponse)
                .exceptionally(this::handleEventsException);
    }

    private void setUpTimeframeComboBox() {
        timeframeComboBox.getItems().addAll("Today", "This Week", "This Month", "This Year");

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> initialFilters = searchParams.getFilters();
        initialFilters.put("dateTimeStart", now.toString());
        searchParams.setFilters(initialFilters);

        timeframeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            LocalDateTime endDate = switch (newValue) {
                case "Today" -> now.plusDays(1);
                case "This Week" -> now.plusWeeks(1);
                case "This Month" -> now.plusMonths(1);
                case "This Year" -> now.plusYears(1);
                default -> now;
            };

            searchParams.updateFilter("dateTimeEnd", endDate.toString());
        });
    }

    private Result<List<UpcomingEvent>> handleEventsResponse(Result<List<UpcomingEvent>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                System.out.println("Failed to fetch upcoming events: " + result.getError());
                return;
            }

            events = result.getData();
            renderTimeline();
        });
        return result;
    }

    private Result<List<UpcomingEvent>> handleEventsException(Throwable throwable) {
        Platform.runLater(() ->
            System.out.println("Failed to fetch upcoming events: " + throwable.getMessage()));
        return new Result<>();
    }

    private void renderTimeline() {
        eventsHBox.getChildren().clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        for (UpcomingEvent event : events) {
            if (event.getDateTime() == null) {
                continue;
            }

            renderEventBox(event, formatter);
        }
    }

    private void renderEventBox(UpcomingEvent event, DateTimeFormatter formatter) {
        VBox eventBox = new VBox();
        eventBox.setAlignment(Pos.CENTER);
        eventBox.setPrefWidth(150);

        // Dot to indicate the event
        Region dot = new Region();
        dot.setPrefSize(10, 10);
        dot.getStyleClass().add("event-dot");

        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label message = new Label(event.getMessage());
        message.setStyle("-fx-text-fill: #555;");

        Label dateTime = new Label(event.getDateTime().format(formatter));
        dateTime.setStyle("-fx-text-fill: gray;");

        eventBox.getChildren().addAll(dot, title, message, dateTime);
        eventsHBox.getChildren().add(eventBox);
    }

    @FXML
    private void refreshEvents() {
        eventsHBox.getChildren().clear();
        loadEvents();
    }
}
