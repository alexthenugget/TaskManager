package com.github.alex_the_nugget.taskhub.taskhub.controllers.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public abstract class CommonTaskFunctionality extends BaseTaskInfoController {
    private static final Logger logger = LoggerFactory.getLogger(CommonTaskFunctionality.class);
    protected static final String EXPIRED_STATUS = "Expired";
    protected static final String COMPLETED_STATUS = "Completed";

    @Override
    protected void initializeUI() {
        super.initializeUI();
        checkAndUpdateExpiredStatus();
    }

    protected void checkAndUpdateExpiredStatus() {
        try {
            if (task.getEndDate() == null) {
                return;
            }

            LocalDate today = LocalDate.now();
            boolean isExpired = task.getEndDate().isBefore(today.atStartOfDay());
            boolean isNotCompleted = !COMPLETED_STATUS.equalsIgnoreCase(task.getStatus());

            if (isExpired && isNotCompleted) {
                handleExpiredTask();
            }
        } catch (Exception e) {
            logger.error("Error checking task expiration", e);
        }
    }

    @Override
    protected void updateStatus() {
        try {
            if (EXPIRED_STATUS.equals(task.getStatus())) {
                return;
            }

            String newStatus = statusComboBox.getValue();
            task.setStatus(newStatus);
            updateTaskStatus(task.getId(), newStatus);
            refreshView();
        } catch (Exception e) {
            logger.error("Error updating task status", e);
        }
    }

    @Override
    protected void updateRating(String rating) {
        try {
            task.setRating(rating);
            updateTaskRating(task.getId(), rating);
            updateRatingImage(rating);
            refreshView();
        } catch (Exception e) {
            logger.error("Error updating task rating", e);
        }
    }

    @Override
    protected void handleExpiredTask() {
        try {
            task.setStatus(EXPIRED_STATUS);
            updateTaskStatus(task.getId(), EXPIRED_STATUS);
            statusComboBox.setDisable(true);
            statusComboBox.setValue(EXPIRED_STATUS);
            refreshView();
        } catch (Exception e) {
            logger.error("Error handling expired task", e);
        }
    }

    protected abstract void updateTaskStatus(int taskId, String status);
    protected abstract void updateTaskRating(int taskId, String rating);
}