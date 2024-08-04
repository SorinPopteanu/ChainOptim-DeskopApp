package org.chainoptim.desktop.core.overview.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.overview.model.UpcomingEvent;
import org.chainoptim.desktop.core.overview.service.UpcomingEventService;
import org.chainoptim.desktop.shared.httphandling.Result;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;


public class TimelineController {

    // Services
    private final UpcomingEventService upcomingEventService;

    // State
    private List<UpcomingEvent> events;

    // FXML
    @FXML
    private Button refreshButton;
    @FXML
    private HBox eventsHBox;

    @Inject
    public TimelineController(UpcomingEventService upcomingEventService) {
        this.upcomingEventService = upcomingEventService;
    }

    public void initializeTimeline() {
        eventsHBox.setSpacing(40);
        eventsHBox.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-border-width: 1; -fx-padding: 8px;");

        upcomingEventService.getUpcomingEventsByOrganizationId(TenantContext.getCurrentUser().getOrganization().getId())
                .thenApply(this::handleEventsResponse)
                .exceptionally(this::handleEventsException);

        renderTimeline();
    }

    private Result<List<UpcomingEvent>> handleEventsResponse(Result<List<UpcomingEvent>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                System.out.println("Failed to fetch upcoming events: " + result.getError());
                return;
            }

            events = result.getData();
        });
        return result;
    }

    private Result<List<UpcomingEvent>> handleEventsException(Throwable throwable) {
        Platform.runLater(() -> {
            System.out.println("Failed to fetch upcoming events: " + throwable.getMessage());
        });
        return new Result<>();
    }

    private void renderTimeline() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        for (UpcomingEvent event : events) {
            VBox eventBox = createEventBox(event, formatter);
            eventsHBox.getChildren().add(eventBox);
        }
    }

    private VBox createEventBox(UpcomingEvent event, DateTimeFormatter formatter) {
        VBox eventBox = new VBox();
        eventBox.setAlignment(Pos.CENTER);
        eventBox.setPrefWidth(150);

        // Dot to indicate the event
        Region dot = new Region();
        dot.setPrefSize(10, 10);
        dot.setStyle("-fx-background-color: #0078d4; -fx-background-radius: 5;"); // Blue dot

        // Title
        Label title = new Label(event.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // Date and time
        Label dateTime = new Label(event.getDateTime().format(formatter));
        dateTime.setStyle("-fx-text-fill: gray;");

        // Message
        Label message = new Label(event.getMessage());
        message.setStyle("-fx-text-fill: #555;");

        // Add components to the event box
        eventBox.getChildren().addAll(dot, title, dateTime, message);
        return eventBox;
    }

    @FXML
    private void refreshEvents() {
        System.out.println("Refreshing events...");
    }
}
