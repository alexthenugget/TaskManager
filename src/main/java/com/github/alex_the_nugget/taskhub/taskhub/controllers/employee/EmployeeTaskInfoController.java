package com.github.alex_the_nugget.taskhub.taskhub.controllers.employee;

import com.github.alex_the_nugget.taskhub.taskhub.controllers.shared.CommonTaskFunctionality;
import com.github.alex_the_nugget.taskhub.taskhub.services.EmployeeService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class EmployeeTaskInfoController extends CommonTaskFunctionality {
    private EmployeeService employeeService;

    @FXML
    Button okButton;

    @FXML
    private void initialize() {
        okButton.setOnAction(event -> {
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    protected void updateTaskStatus(int taskId, String status) {
            employeeService.updateTaskStatus(taskId, status);
    }

    @Override
    protected void updateTaskRating(int taskId, String rating) {
            employeeService.updateTaskRating(taskId, rating);
    }
}