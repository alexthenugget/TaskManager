package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import com.github.alex_the_nugget.taskhub.taskhub.services.ManagerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class ManagerAssignTasksController {

    private final ManagerService managerService = new ManagerService();

    private String userLogin;

    @FXML
    private ComboBox<String> chooseEmployee;

    @FXML
    private ComboBox<String> chooseTag;

    @FXML
    private TextField taskName;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private Button assignTaskButton;

    @FXML
    private TextField taskDescription;

    @FXML
    public void handleChooseEmployeeComboBox(ActionEvent event) {
        checkSelections();
    }

    @FXML
    public void handleChooseTagComboBox(ActionEvent event) {
        checkSelections();
    }

    @FXML
    private Label response;

    public void setUserLogin(String login) {
        this.userLogin = login;
        chooseEmployee.getItems().addAll(managerService.returnAnEmployeeList(userLogin));
    }

    @FXML
    public void initialize() {
        chooseTag.getItems().addAll(
                "Work",
                "Call",
                "Meeting",
                "Bug",
                "Frontend",
                "Backend",
                "Mobile",
                "Testing",
                "DevOps",
                "Design",
                "Management"
        );
        assignTaskButton.setDisable(true);
        chooseEmployee.valueProperty().addListener((obs, oldVal, newVal) -> checkSelections());
        chooseTag.valueProperty().addListener((obs, oldVal, newVal) -> checkSelections());
    }

    private void checkSelections() {
        assignTaskButton.setDisable(chooseEmployee.getValue() == null || chooseTag.getValue() == null);
    }

    @FXML
    public void handleAssignTaskButton(ActionEvent event) {
        String selectedEmployee = chooseEmployee.getValue();
        String selectedTag = chooseTag.getValue();
        String selectedTaskName = taskName.getText();
        String selectedStartDate = startDate.getValue().toString();
        String selectedEndDate = endDate.getValue().toString();
        String selectedTaskDescription = taskDescription.getText();

        if (selectedEmployee.isEmpty() || selectedTag.isEmpty() || selectedTaskName.isEmpty() ||
                selectedStartDate.isEmpty() || selectedEndDate.isEmpty() || selectedTaskDescription.isEmpty()) {
            response.setText("There is an empty field");
        }
        else {
            updateUserTaskInfo(userLogin, selectedEmployee, selectedTag, selectedTaskName, selectedStartDate,
                    selectedEndDate, selectedTaskDescription);
            try {
                FXMLLoader loader = new FXMLLoader(getClass()
                        .getResource("/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"));
                Parent root = loader.load();
                ManagerAssignTasksController managerAssignTasksController = loader.getController();
                managerAssignTasksController.setUserLogin(this.userLogin);
                managerAssignTasksController.setResponseText("Task successfully assigned!");
                Stage stage = (Stage) assignTaskButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserTaskInfo(String managerLogin, String employee, String tag, String taskName,
                                    String startDate, String endDate, String taskDescription) {
        managerService.addNewUserTask(managerLogin, employee, tag, taskName, startDate, endDate, taskDescription);
    }

    private void setResponseText(String text){
        response.setText(text);
    }

}
