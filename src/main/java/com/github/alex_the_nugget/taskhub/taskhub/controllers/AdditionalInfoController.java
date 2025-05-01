package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import com.github.alex_the_nugget.taskhub.taskhub.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdditionalInfoController {

    private final AuthService authService = new AuthService();

    @FXML
    private ComboBox<String> position;

    @FXML
    private ComboBox<String> department;

    @FXML
    private Button startButton;

    private String userLogin;

    @FXML
    public void handlePositionComboBox(ActionEvent event) {
        checkSelections();
    }

    @FXML
    public void handleDepartmentComboBox(ActionEvent event) {
        checkSelections();
    }

    public void setUserLogin(String login) {
        this.userLogin = login;
    }

    @FXML
    public void initialize() {
        position.getItems().addAll("Manager", "Employee");
        department.getItems().addAll(
                "Frontend Development",
                "Backend Development",
                "Mobile Development",
                "Testing Development",
                "DevOps Development",
                "Design",
                "Product Management"
        );

        startButton.setDisable(true);
        position.valueProperty().addListener((_, _, _) -> checkSelections());
        department.valueProperty().addListener((obs, oldVal, newVal) -> checkSelections());
    }

    private void checkSelections() {
        startButton.setDisable(position.getValue() == null || department.getValue() == null);
    }

    @FXML
    public void handleStartButton(ActionEvent event) {
        String selectedPosition = position.getValue();
        String selectedDepartment = department.getValue();

        updateUserInfo(userLogin, selectedPosition, selectedDepartment);

        try {
            String fxmlFile = selectedPosition.equals("Manager")
                    ? "/com/github/alex_the_nugget/taskhub/taskhub/ManagerAssignTasksPage.fxml"
                    : "/com/github/alex_the_nugget/taskhub/taskhub/EmployeeCheckTasksPage.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (selectedPosition.equals("Manager")) {
                ManagerAssignTasksController managerController = loader.getController();
                managerController.setUserLogin(userLogin);
            } else {
                EmployeeCheckTasksController employeeCheckTasksController = loader.getController();
                employeeCheckTasksController.setUserLogin(userLogin);
            }

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserInfo(String login, String position, String department) {
        authService.updateAdditionalInfo(login, position, department);
    }
}
