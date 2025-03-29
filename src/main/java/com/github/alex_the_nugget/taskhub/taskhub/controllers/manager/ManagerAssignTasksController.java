package com.github.alex_the_nugget.taskhub.taskhub.controllers.manager;

import com.github.alex_the_nugget.taskhub.taskhub.services.ManagerService;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ManagerAssignTasksController {
    private static final Logger logger = LoggerFactory.getLogger(ManagerAssignTasksController.class);

    private final ManagerService managerService = new ManagerService();
    private final UserSession userSession = UserSession.getInstance();

    @FXML private ComboBox<String> chooseEmployee;
    @FXML private ComboBox<String> chooseTag;
    @FXML private TextField taskName;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private Button assignTaskButton;
    @FXML private TextField taskDescription;
    @FXML private ImageView imageMyTasks;
    @FXML private Label response;

    @FXML
    public void initialize() {
        try {
            String userLogin = userSession.getLogin();

            chooseEmployee.getItems().addAll(managerService.returnAnEmployeeList());
            chooseTag.getItems().addAll(
                    "Work", "Call", "Meeting", "Bug", "Frontend",
                    "Backend", "Mobile", "Testing", "DevOps", "Design", "Management"
            );
            assignTaskButton.setDisable(true);
            chooseEmployee.valueProperty().addListener((obs, oldVal, newVal) -> checkSelections());
            chooseTag.valueProperty().addListener((obs, oldVal, newVal) -> checkSelections());
        } catch (Exception e) {
            logger.error("Error during controller initialization", e);
            response.setText("Error loading employee list");
        }
    }

    private void checkSelections() {
        assignTaskButton.setDisable(chooseEmployee.getValue() == null || chooseTag.getValue() == null);
    }

    @FXML
    public void handleAssignTaskButton(ActionEvent event) {
        try {
            String userLogin = userSession.getLogin();
            String selectedEmployee = chooseEmployee.getValue();
            String selectedTag = chooseTag.getValue();
            String selectedTaskName = taskName.getText();
            String selectedStartDate = startDate.getValue() != null ? startDate.getValue().toString() : "";
            String selectedEndDate = endDate.getValue() != null ? endDate.getValue().toString() : "";
            String selectedTaskDescription = taskDescription.getText();

            if (selectedEmployee == null || selectedEmployee.isEmpty() ||
                    selectedTag == null || selectedTag.isEmpty() ||
                    selectedTaskName.isEmpty() || selectedStartDate.isEmpty() ||
                    selectedEndDate.isEmpty() || selectedTaskDescription.isEmpty()) {

                response.setText("There is an empty field");
                logger.warn("Attempt to assign task with empty fields");
                return;
            }

            updateUserTaskInfo(userLogin, selectedEmployee, selectedTag, selectedTaskName,
                    selectedStartDate, selectedEndDate, selectedTaskDescription);

            try {
                FXMLLoader loader = new FXMLLoader(getClass()
                        .getResource("/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"));
                Parent root = loader.load();
                ManagerAssignTasksController managerAssignTasksController = loader.getController();
                managerAssignTasksController.setResponseText("Task successfully assigned!");

                Stage stage = (Stage) assignTaskButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Failed to load AssignTasksPage FXML", e);
                response.setText("Error navigating to page");
            }
        } catch (Exception e) {
            logger.error("Unexpected error during task assignment", e);
            response.setText("Error assigning task");
        }
    }

    @FXML
    public void handleMyTasksClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/github/alex_the_nugget/taskhub/taskhub/ManagerCheckTasksPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) imageMyTasks.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load ManagerCheckTasksPage FXML", e);
            response.setText("Error loading tasks page");
        }
    }

    @FXML
    public void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/github/alex_the_nugget/taskhub/taskhub/ManagerStatisticsPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) imageMyTasks.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load ManagerStatisticsPage FXML", e);
            response.setText("Error loading statistics page");
        }
    }

    private void updateUserTaskInfo(String managerLogin, String employee, String tag, String taskName,
                                    String startDate, String endDate, String taskDescription) {
        try {
            managerService.addNewUserTask(employee, tag, taskName, startDate, endDate, taskDescription);
        } catch (Exception e) {
            logger.error("Failed to assign task. Manager: {}, Employee: {}, Task: {}",
                    managerLogin, employee, taskName, e);
            throw e;
        }
    }

    private void setResponseText(String text) {
        response.setText(text);
    }
}