package com.github.alex_the_nugget.taskhub.taskhub.controllers.shared;

import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseCheckTasksController {
    private static final Logger logger = LoggerFactory.getLogger(BaseCheckTasksController.class);

    protected final UserSession userSession = UserSession.getInstance();

    @FXML
    protected Label response;

    @FXML
    protected HBox columnsContainer;

    @FXML
    protected ImageView imageStatistics;

    private static final String COLUMN_STYLE = "-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1px; -fx-border-radius: 5px;";
    private static final String TAG_LABEL_STYLE = "-fx-padding: 5 10; -fx-background-color: #e56b6f; -fx-text-fill: white; -fx-border-radius: 5px;";
    private static final String CARD_STYLE = "-fx-padding: 10; -fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 5px;";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    protected abstract List<Task> getTasksList();

    protected void loadTasks() {
        try {
            if (userSession.getLogin() == null) {
                response.setText("User not logged in");
                return;
            }

            List<Task> tasks = getTasksList();
            if (tasks == null || tasks.isEmpty()) {
                response.setText("No tasks found");
                return;
            }
            Map<String, List<Task>> tasksByTag = tasks.stream()
                    .collect(Collectors.groupingBy(
                            task -> task.getTag() != null ? task.getTag() : "UNTAGGED"
                    ));
            columnsContainer.getChildren().clear();
            columnsContainer.setStyle("-fx-background-color: #f0f0f0;");

            tasksByTag.forEach((tag, taskList) -> {
                VBox column = createTagColumn(tag, taskList);
                columnsContainer.getChildren().add(column);
            });

        } catch (Exception e) {
            logger.error("Error loading tasks for user {}", userSession.getLogin(), e);
            response.setText("Error loading tasks");
            e.printStackTrace();
        }
    }

    private VBox createTagColumn(String tagName, List<Task> tasks) {
        VBox column = new VBox();
        column.setSpacing(10);
        column.setPrefWidth(250);
        column.setStyle(COLUMN_STYLE);

        Label tagLabel = new Label(tagName);
        tagLabel.setFont(Font.font("Lato", FontWeight.BOLD, 16));
        tagLabel.setStyle(TAG_LABEL_STYLE);
        tagLabel.setMaxWidth(Double.MAX_VALUE);

        column.getChildren().add(tagLabel);

        tasks.forEach(task -> column.getChildren().add(createTaskCard(task)));

        return column;
    }

    private Pane createTaskCard(Task task) {
        VBox card = new VBox(5);
        card.setStyle(CARD_STYLE);
        card.setPrefWidth(230);

        Label titleLabel = new Label(task.getTaskName());
        titleLabel.setFont(Font.font("Lato", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        Label statusLabel = createStatusLabel(task.getStatus());
        Label datesLabel = createDatesLabel(task.getStartDate(), task.getEndDate());

        card.getChildren().addAll(titleLabel, statusLabel, datesLabel);
        card.setOnMouseClicked(e -> handleTaskClick(task));

        return card;
    }

    private Label createStatusLabel(String status) {
        Label label = new Label("Status: " + status);
        label.setFont(Font.font("Lato", 12));

        switch(status.toLowerCase()) {
            case "new": label.setStyle("-fx-text-fill: #e75055;"); break;
            case "in progress": label.setStyle("-fx-text-fill: #f4c064;"); break;
            case "completed": label.setStyle("-fx-text-fill: #9dd15c;"); break;
            default: label.setStyle("-fx-text-fill: black;");
        }

        return label;
    }

    private Label createDatesLabel(LocalDateTime startDate, LocalDateTime endDate) {
        Label label = new Label(String.format(
                "Start date: %s\nEnd date: %s",
                formatDate(startDate),
                formatDate(endDate)
        ));
        label.setFont(Font.font("Lato", 12));
        return label;
    }

    private String formatDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "not present";
    }

    protected abstract void handleTaskClick(Task task);

    @FXML
    protected abstract void checkStatistics();
}