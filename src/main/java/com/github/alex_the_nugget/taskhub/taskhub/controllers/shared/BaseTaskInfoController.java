package com.github.alex_the_nugget.taskhub.taskhub.controllers.shared;

import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public abstract class BaseTaskInfoController {
    private static final Logger logger = LoggerFactory.getLogger(BaseTaskInfoController.class);
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    protected String userLogin;
    protected Task task;
    protected Consumer<Void> refreshCallback;

    @FXML protected Label taskNameLabel;
    @FXML protected Label managerLabel;
    @FXML protected Label tagLabel;
    @FXML protected Label datesLabel;
    @FXML protected TextArea description;
    @FXML protected ComboBox<String> statusComboBox;
    @FXML protected HBox ratingBox;
    @FXML protected Button okButton;
    @FXML protected Button okRatingButton;
    @FXML protected Button goodButton;
    @FXML protected Button badButton;
    @FXML protected VBox mainContainer;

    protected ImageView ratingImageView;

    public void setUserLogin(String login) {
        this.userLogin = login;
    }

    public void setTask(Task task) {
        this.task = task;
        initializeUI();
    }

    public void setRefreshCallback(Consumer<Void> refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    protected void initializeUI() {
        try {
            if (ratingBox == null) {
                throw new IllegalStateException("FXML elements not properly initialized. Check FXML file.");
            }

            taskNameLabel.setText(task.getTaskName());
            managerLabel.setText(task.getManagerName());
            tagLabel.setText(task.getTag());
            datesLabel.setText(String.format("Start: %s End: %s",
                    formatDate(task.getStartDate()),
                    formatDate(task.getEndDate())));
            description.setText(task.getDescription());
            description.setEditable(false);

            statusComboBox.getItems().addAll("New", "In Progress", "Completed");
            statusComboBox.setValue(task.getStatus());
            statusComboBox.setOnAction(e -> updateStatus());

            initializeRatingSystem();
            updateRatingImage(task.getRating());
        } catch (Exception e) {
            logger.error("Error initializing UI", e);
            throw e;
        }
    }

    protected abstract void updateStatus();
    protected abstract void updateRating(String rating);
    protected abstract void handleExpiredTask();

    protected void initializeRatingSystem() {
        try {
            if (ratingBox == null) {
                logger.error("RatingBox is not initialized!");
                return;
            }

            ratingBox.getChildren().clear();

            okRatingButton.setOnAction(e -> updateRating("ok"));
            goodButton.setOnAction(e -> updateRating("good"));
            badButton.setOnAction(e -> updateRating("bad"));

            ratingBox.getChildren().addAll(okRatingButton, goodButton, badButton);

            ratingImageView = new ImageView();
            ratingImageView.setFitHeight(30);
            ratingImageView.setFitWidth(30);
            ratingBox.getChildren().add(ratingImageView);
        } catch (Exception e) {
            logger.error("Error initializing rating system", e);
        }
    }

    protected void updateRatingImage(String rating) {
        try {
            logger.debug("Attempting to load rating image for: {}", rating);

            String imagePath = switch (rating != null ? rating.toLowerCase() : "") {
                case "good" -> "/com/github/alex_the_nugget/taskhub/taskhub/images/thumbsup.png";
                case "ok" -> "/com/github/alex_the_nugget/taskhub/taskhub/images/smile.png";
                case "bad" -> "/com/github/alex_the_nugget/taskhub/taskhub/images/poop.png";
                default -> null;
            };

            if (imagePath == null) {
                logger.debug("No image for rating: {}", rating);
                ratingImageView.setImage(null);
                return;
            }

            InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) {
                logger.error("Image not found at: {}", imagePath);
                ratingImageView.setImage(null);
            } else {
                Image image = new Image(stream);
                logger.debug("Image loaded successfully. Width: {}", image.getWidth());
                ratingImageView.setImage(image);
            }
        } catch (Exception e) {
            logger.error("Error loading rating image", e);
            ratingImageView.setImage(null);
        }
    }

    protected String formatDate(LocalDateTime date) {
        return date != null
                ? date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
                : "N/A";
    }

    protected void refreshView() {
        if (refreshCallback != null) {
            refreshCallback.accept(null);
        }
    }
}